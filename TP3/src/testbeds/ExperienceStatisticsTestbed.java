package testbeds;

import entityClasses.LessonExperience;
import entityClasses.ExperienceStatistics;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs P01-P15 against the statistics calculation component using fixed records.
 * No application database or JavaFX screen is needed.
 */
public class ExperienceStatisticsTestbed {
    /** Passed case groups. */
    private static int passed;
    /** Failed case groups. */
    private static int failed;

    /** Creates the command-line test runner. */
    private ExperienceStatisticsTestbed() { }

    /**
     * Runs the planned calculation cases and reports each group separately.
     * @param args unused
     * @throws AssertionError if a case fails
     */
    public static void main(String[] args) {
        LessonExperience a = record(101, "30.00", "Alex:20.00\nBlair:25.00");
        LessonExperience b = record(102, "50.00", "Alex:30.00\nBlair:35.00");
        LessonExperience c = record(103, "40.00", "Casey:10.00");
        run("P01 two attempts", () -> result(List.of(a,b), "40.00", "110.00", 101L,102L));
        run("P02 three attempts", () -> result(List.of(a,b,c), "40.00", "120.00", 101L,102L,103L));
        run("P03 halfway rounding", () -> result(List.of(record(1,"0.01","A:0.01"),
                record(2,"0.02","A:0.02")), "0.02", "0.03", 1L,2L));
        run("P04 repeating mean", () -> result(List.of(record(1,"0.01","A:0.01"),
                record(2,"0.01","A:0.01"), record(3,"0.02","A:0.02")), "0.01", "0.04", 1L,2L,3L));
        run("P05 zero effort", () -> result(List.of(record(1,"1.00","A:0.00"),
                record(2,"3.00","A:0.00")), "2.00", "0.00", 1L,2L));
        run("P06 maximum values", () -> result(List.of(
                record(1,"9999999999.99","A:9999999999.99\nB:9999999999.99"),
                record(2,"9999999999.99","A:9999999999.99\nB:9999999999.99")),
                "9999999999.99", "39999999999.96", 1L,2L));
        run("P07 effort line format", () -> result(List.of(
                record(1,"1.10","Team:A:0.10\r\nBlair:0.20"),
                record(2,"1.30","Team:A:0.30\r\nBlair:0.40")), "1.20", "1.00", 1L,2L));
        run("P08 selection size", () -> {
            rejects(() -> ExperienceStatistics.summarize(null), "two");
            rejects(() -> ExperienceStatistics.summarize(List.of()), "two");
            rejects(() -> ExperienceStatistics.summarize(List.of(a)), "two");
        });
        run("P09 missing record", () -> rejects(
                () -> ExperienceStatistics.summarize(Arrays.asList(a,null)), "missing"));
        run("P10 saved IDs", () -> {
            rejects(() -> ExperienceStatistics.summarize(List.of(b,record(0,"1","A:1"))), "saved");
            rejects(() -> ExperienceStatistics.summarize(List.of(b,record(-1,"1","A:1"))), "saved");
        });
        run("P11 duplicate IDs", () -> {
            rejects(() -> ExperienceStatistics.summarize(List.of(a,a)), "duplicate");
            rejects(() -> ExperienceStatistics.summarize(List.of(a,record(101,"30","A:1"))), "duplicate");
        });
        run("P12 mixed lessons", () -> rejects(() -> ExperienceStatistics.summarize(List.of(a,
                new LessonExperience(104,8,"alice","Check files","Use checklist",
                        new BigDecimal("1"),"A:1"))), "same lesson"));
        run("P13 selection order", () -> result(List.of(b,a), "40.00", "110.00", 102L,101L));
        run("P14 independent result", () -> {
            List<LessonExperience> input = new ArrayList<>(List.of(a,b));
            ExperienceStatistics summary = ExperienceStatistics.summarize(input);
            input.clear(); // The result must keep its own source IDs.
            check(summary.getSourceIds().equals(List.of(101L,102L)), "Result follows changed input");
            try {
                summary.getSourceIds().add(999L);
                throw new AssertionError("Source IDs must be read-only");
            } catch (UnsupportedOperationException expected) { }
            check(summary.getAttemptCount() == 2, "Count changed");
            check(summary.getMeanElapsedMinutes().equals(new BigDecimal("40.00")), "Mean changed");
            check(summary.getTotalEffortMinutes().equals(new BigDecimal("110.00")), "Effort changed");
        });
        run("P15 preserve sources", () -> {
            List<LessonExperience> input = new ArrayList<>(List.of(a,b));
            List<String> before = snapshot(input); // Include fields unrelated to the calculation.
            ExperienceStatistics.summarize(input);
            check(input.size() == 2 && input.get(0) == a && input.get(1) == b, "Selection changed");
            check(before.equals(snapshot(input)), "Source fields changed");
            List<LessonExperience> duplicate = new ArrayList<>(List.of(a,a));
            List<String> original = snapshot(duplicate);
            rejects(() -> ExperienceStatistics.summarize(duplicate), "duplicate");
            check(duplicate.size() == 2 && duplicate.get(0) == a && duplicate.get(1) == a,
                    "Rejected selection changed");
            check(original.equals(snapshot(duplicate)), "Rejected source fields changed");
        });
        System.out.println("Case groups passed: " + passed + "; failed: " + failed);
        if (failed != 0) throw new AssertionError("Statistics tests failed: " + failed);
    }

    /** Builds a validated, synthetic attempt for lesson 7.
     * @param id saved identity or invalid identity under test
     * @param elapsed elapsed minute text
     * @param effort member effort lines
     * @return validated attempt
     */
    private static LessonExperience record(long id, String elapsed, String effort) {
        return new LessonExperience(id,7,"alice","Check files","Use checklist",new BigDecimal(elapsed),effort);
    }

    /** Checks every result field against hand-calculated expectations, including decimal scale.
     * @param input selected attempts
     * @param mean expected mean text
     * @param effort expected effort text
     * @param ids expected ordered source identities
     */
    private static void result(List<LessonExperience> input, String mean, String effort, Long... ids) {
        ExperienceStatistics summary = ExperienceStatistics.summarize(input);
        check(summary.getLessonId() == 7, "Wrong lesson");
        check(summary.getAttemptCount() == ids.length, "Wrong count");
        check(summary.getMeanElapsedMinutes().equals(new BigDecimal(mean)), "Wrong mean");
        check(summary.getTotalEffortMinutes().equals(new BigDecimal(effort)), "Wrong effort");
        check(summary.getSourceIds().equals(Arrays.asList(ids)), "Wrong source IDs");
    }

    /** Requires the documented exception and a message identifying the invalid input.
     * @param action invalid operation
     * @param message required error-message fragment
     */
    private static void rejects(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage() != null && expected.getMessage().toLowerCase().contains(message),
                    "Unhelpful error: " + expected.getMessage());
            return;
        }
        throw new AssertionError("Invalid selection was accepted");
    }

    /** Saves every field, not only the values used by the calculator.
     * @param input records to inspect
     * @return field snapshots in input order
     */
    private static List<String> snapshot(List<LessonExperience> input) {
        List<String> values = new ArrayList<>();
        for (LessonExperience item : input) {
            values.add(item.getId() + "|" + item.getLessonId() + "|" + item.getContributor()
                    + "|" + item.getWhatDone() + "|" + item.getHowDone() + "|"
                    + item.getElapsedMinutes().toPlainString() + "|" + item.getMemberEffort());
        }
        return values;
    }

    /** Reports one planned case group and continues so other failures remain visible.
     * @param name case ID and description
     * @param test checks for this case
     */
    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (AssertionError | RuntimeException error) {
            failed++;
            System.out.println("FAIL: " + name + " - " + error);
        }
    }

    /** Stops the current case when an expected result does not match.
     * @param condition whether the expectation holds
     * @param message failure description
     * @throws AssertionError if the condition is false
     */
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
