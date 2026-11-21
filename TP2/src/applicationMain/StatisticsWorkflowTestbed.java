package applicationMain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import database.Database;
import database.Database.ExperienceSummary;
import entityClasses.Category;
import entityClasses.LessonsLearned;
import entityClasses.User;

/** Exercises the database workflow for generating and sharing lesson statistics. */
public final class StatisticsWorkflowTestbed {
	private StatisticsWorkflowTestbed() { }

	/**
	 * Tests curator access, selection, saved calculations, visibility and persistence on a fresh H2 database.
	 * @param args unused
	 * @throws Exception if setup, storage, cleanup or an assertion fails
	 */
	public static void main(String[] args) throws Exception {
		Path folder = Files.createTempDirectory("tp3-statistics-");
		System.setProperty("foundation.db.url", "jdbc:h2:" + folder.resolve("statistics"));
		Database db = new Database();
		try {
			db.connectToDatabase();
			register(db, "alice", true, false);
			register(db, "bob", true, false);
			register(db, "carol", false, true);
			check(db.addLesson(new LessonsLearned("alice", "Deploy safely", "Use a checklist.", Category.values()[0])),
					"create fixture lesson");
			check(db.addLesson(new LessonsLearned("bob", "Review", "Use a checklist.", Category.values()[0])),
					"create second lesson");
			int lessonId = db.getLessonByAuthor("alice").getLessons().get(0).getLessonID();
			int otherLesson = db.getLessonByAuthor("bob").getLessons().get(0).getLessonID();
			long a = db.addExperience("alice", lessonId, "First run", "Checklist", "30", "Alex:20\nBlair:25");
			long b = db.addExperience("alice", lessonId, "Second run", "Checklist", "50", "Alex:30\nBlair:35");
			long c = db.addExperience("bob", otherLesson, "First run", "Checklist", "20", "Bob:20");
			List<Long> selection = List.of(a, b);
			check(db.getCuratorExperiences("carol", lessonId).size() == 2, "curator loads two attempts");
			rejects(() -> db.getCuratorExperiences("alice", lessonId), "contributor cannot use curator reads");
			rejects(() -> db.setExperienceSummaryVisible("carol", lessonId, true), "cannot publish before saving");
			rejects(() -> db.saveExperienceSummary("alice", lessonId, selection), "contributor cannot save a summary");
			rejects(() -> db.saveExperienceSummary("carol", lessonId, List.of(a)), "requires two attempts");
			rejects(() -> db.saveExperienceSummary("carol", lessonId, List.of(a, a)), "rejects duplicate attempts");
			rejects(() -> db.saveExperienceSummary("carol", lessonId, List.of(a, c)), "rejects a different lesson");

			db.saveExperienceSummary("carol", lessonId, selection);
			ExperienceSummary saved = db.getExperienceSummary("carol", lessonId, true);
			check(saved != null && saved.attemptCount() == 2 && !saved.visible()
					&& saved.meanElapsedMinutes().toPlainString().equals("40.00")
					&& saved.totalEffortMinutes().toPlainString().equals("110.00"), "saves correct totals, hidden by default");
			check(db.getExperienceSummary("alice", lessonId, false) == null, "owner cannot read a hidden summary");
			rejects(() -> db.getExperienceSummary("alice", lessonId, true), "contributor cannot open curator summary");
			rejects(() -> db.setExperienceSummaryVisible("alice", lessonId, true), "contributor cannot publish");
			db.setExperienceSummaryVisible("carol", lessonId, true);
			check(db.getExperienceSummary("alice", lessonId, false).visible(), "owner can read a published summary");
			rejects(() -> db.getExperienceSummary("bob", lessonId, false), "another contributor cannot read it");

			db.closeConnection();
			db.connectToDatabase();
			check(db.getExperienceSummary("alice", lessonId, false).totalEffortMinutes().toPlainString().equals("110.00"),
					"published summary survives reconnect");
			db.setExperienceSummaryVisible("carol", lessonId, false);
			check(db.getExperienceSummary("alice", lessonId, false) == null
					&& db.getExperienceSummary("carol", lessonId, true) != null, "hiding preserves the curator's summary");
			db.setExperienceSummaryVisible("carol", lessonId, true);
			db.saveExperienceSummary("carol", lessonId, selection);
			check(db.getExperienceSummary("alice", lessonId, false) == null, "replacement starts hidden");
			check(db.getExperiences("alice", lessonId).stream().map(record -> record.getId()).toList().equals(selection),
					"summary operations preserve the lesson experience records");
		} finally {
			db.closeConnection();
			try (var files = Files.list(folder)) {
				for (Path file : files.toList()) Files.delete(file);
			}
			Files.delete(folder);
		}
	}

	private static void register(Database db, String username, boolean contributor, boolean curator) throws Exception {
		db.register(new User(username, "TestPass1!", "Test", "", "User", "", username + "@example.com",
				false, contributor, curator));
	}

	private static void check(boolean passes, String name) {
		if (!passes) throw new AssertionError(name);
		System.out.println("PASS: " + name);
	}

	private static void rejects(Action action, String name) throws Exception {
		try {
			action.run();
		} catch (IllegalArgumentException expected) {
			System.out.println("PASS: " + name);
			return;
		}
		throw new AssertionError(name);
	}

	@FunctionalInterface
	private interface Action {
		void run() throws Exception;
	}
}
