package guiStatistics;

import java.sql.SQLException;
import java.util.List;

import applicationMain.FoundationsMain;
import database.Database;
import database.Database.ExperienceSummary;
import entityClasses.ExperienceStatistics;
import entityClasses.LessonExperience;
import entityClasses.LessonsLearned;
import entityClasses.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Shows a lesson's statistics and gives curators controls to create and share a summary. */
public final class ViewStatistics {
	private ViewStatistics() { }

	/**
	 * Opens the statistics dialog for the current role and selected lesson.
	 * @param owner parent window
	 * @param user signed-in account
	 * @param lesson selected lesson
	 * @param curatorView whether the user entered through the curator workflow
	 */
	public static void show(Window owner, User user, LessonsLearned lesson, boolean curatorView) {
		Database database = FoundationsMain.database;
		String username = user.getUserName();
		int lessonId = lesson.getLessonID();
		try {
			ExperienceSummary saved = database.getExperienceSummary(username, lessonId, curatorView);
			if (!curatorView) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION,
						saved == null ? "No statistics are available for this lesson." : describe(saved));
				alert.initOwner(owner);
				alert.setHeaderText(lesson.getLessonTitle());
				alert.showAndWait();
				return;
			}

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.initOwner(owner);
			dialog.setTitle("Experience Statistics");
			dialog.setHeaderText(lesson.getLessonTitle());
			dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

			ListView<LessonExperience> attempts = new ListView<>();
			attempts.setPrefHeight(155);
			attempts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			attempts.setCellFactory(list -> new ListCell<>() {
				@Override
				protected void updateItem(LessonExperience attempt, boolean empty) {
					super.updateItem(attempt, empty);
					setText(empty || attempt == null ? null : "#" + attempt.getId() + "  "
							+ attempt.getContributor() + "  |  " + attempt.getElapsedMinutes()
							+ " min  |  " + attempt.getWhatDone());
				}
			});
			attempts.getItems().setAll(database.getCuratorExperiences(username, lessonId));

			Label savedText = new Label(describeForCurator(saved));
			Label preview = new Label("No preview yet.");
			Label status = new Label();
			status.setWrapText(true);
			Button calculate = new Button("Preview");
			Button save = new Button("Save Summary");
			Button publish = new Button("Show to Contributor");
			Button hide = new Button("Hide from Contributor");
			save.setDisable(true);
			publish.setDisable(saved == null || saved.visible());
			hide.setDisable(saved == null || !saved.visible());

			// Changing the selection invalidates the preview the curator reviewed.
			attempts.getSelectionModel().getSelectedItems().addListener(
					(javafx.collections.ListChangeListener<LessonExperience>) change -> {
						preview.setText("No preview yet.");
						save.setDisable(true);
						status.setText("");
					});
			calculate.setOnAction(event -> run(status, () -> {
				ExperienceStatistics result = ExperienceStatistics.summarize(
						attempts.getSelectionModel().getSelectedItems());
				preview.setText(format(result.getAttemptCount(), result.getMeanElapsedMinutes(),
						result.getTotalEffortMinutes()));
				save.setDisable(false);
			}));
			save.setOnAction(event -> run(status, () -> {
				List<Long> ids = attempts.getSelectionModel().getSelectedItems().stream()
						.map(LessonExperience::getId).toList();
				database.saveExperienceSummary(username, lessonId, ids);
				savedText.setText(describeForCurator(database.getExperienceSummary(username, lessonId, true)));
				save.setDisable(true);
				publish.setDisable(false);
				hide.setDisable(true);
				status.setText("Summary saved.");
			}));
			publish.setOnAction(event -> run(status, () -> {
				database.setExperienceSummaryVisible(username, lessonId, true);
				savedText.setText(describeForCurator(database.getExperienceSummary(username, lessonId, true)));
				publish.setDisable(true);
				hide.setDisable(false);
				status.setText("Summary is visible to the contributor.");
			}));
			hide.setOnAction(event -> run(status, () -> {
				database.setExperienceSummaryVisible(username, lessonId, false);
				savedText.setText(describeForCurator(database.getExperienceSummary(username, lessonId, true)));
				publish.setDisable(false);
				hide.setDisable(true);
				status.setText("Summary is hidden from the contributor.");
			}));
			dialog.getDialogPane().setContent(new VBox(10,
					new Label("Select at least two attempts (Shift or Command/Ctrl-click)."),
					attempts, new HBox(10, calculate, save), preview, savedText,
					new HBox(10, publish, hide), status));
			dialog.getDialogPane().setPrefWidth(580);
			dialog.showAndWait();
		} catch (SQLException | IllegalArgumentException error) {
			Alert alert = new Alert(Alert.AlertType.ERROR, message(error));
			alert.initOwner(owner);
			alert.showAndWait();
		}
	}

	private static String describeForCurator(ExperienceSummary saved) {
		return saved == null ? "No saved summary." :
				"Saved summary (" + (saved.visible() ? "Visible" : "Hidden") + ")\n" + describe(saved);
	}

	private static String describe(ExperienceSummary saved) {
		return format(saved.attemptCount(), saved.meanElapsedMinutes(), saved.totalEffortMinutes());
	}

	private static String format(int count, java.math.BigDecimal mean, java.math.BigDecimal effort) {
		return "Attempts: " + count + "\nAverage elapsed time: " + mean.toPlainString()
				+ " min\nTotal member effort: " + effort.toPlainString() + " person-min";
	}

	private static void run(Label status, StatisticsAction action) {
		status.setText("");
		try {
			action.run();
		} catch (SQLException | IllegalArgumentException error) {
			status.setText(message(error));
		}
	}

	private static String message(Exception error) {
		return error instanceof SQLException ? "Could not complete the statistics operation. Try again."
				: error.getMessage();
	}

	@FunctionalInterface
	private interface StatisticsAction {
		void run() throws SQLException;
	}
}
