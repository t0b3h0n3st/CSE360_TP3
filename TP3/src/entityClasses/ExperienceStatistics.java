package entityClasses;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An immutable calculation for two or more saved attempts at the same lesson.
 * This HW3 prototype calculates values only; authorization and saving belong to
 * the future curator workflow. Mean elapsed time and person-minutes are separate measures.
 */
public final class ExperienceStatistics {
    /** Common lesson ID. */
    private final int lessonId;
    /** Rounded mean elapsed minutes. */
    private final BigDecimal meanElapsedMinutes;
    /** Sum of all selected member-effort lines. */
    private final BigDecimal totalEffortMinutes;
    /** Saved attempt IDs in selection order. */
    private final List<Long> sourceIds;

    /**
     * Keeps a separate, read-only record of the selected sources.
     * @param lessonId common lesson ID
     * @param meanElapsedMinutes rounded mean elapsed time
     * @param totalEffortMinutes summed person-minutes
     * @param sourceIds ordered source IDs to copy
     */
    private ExperienceStatistics(int lessonId, BigDecimal meanElapsedMinutes,
            BigDecimal totalEffortMinutes, List<Long> sourceIds) {
        this.lessonId = lessonId;
        this.meanElapsedMinutes = meanElapsedMinutes;
        this.totalEffortMinutes = totalEffortMinutes;
        this.sourceIds = List.copyOf(sourceIds);
    }

    /**
     * Combines validated attempts without changing the records or selection.
     * The same member's effort counts again for each separate attempt.
     * @param selection at least two saved attempts belonging to one lesson
     * @return count, mean elapsed minutes, total person-minutes and ordered source IDs
     * @throws IllegalArgumentException for missing records, fewer than two records,
     *         nonpositive IDs, duplicate IDs or mixed lessons
     */
    public static ExperienceStatistics summarize(List<LessonExperience> selection) {
        if (selection == null || selection.size() < 2)
            throw new IllegalArgumentException("Select at least two saved attempts.");
        List<LessonExperience> records = new ArrayList<>(selection);
        Set<Long> seen = new HashSet<>();
        List<Long> ids = new ArrayList<>();
        int lessonId = 0;
        BigDecimal elapsed = BigDecimal.ZERO;
        BigDecimal effort = BigDecimal.ZERO;
        for (LessonExperience record : records) {
            if (record == null)
                throw new IllegalArgumentException("A selected attempt is missing.");
            if (record.getId() <= 0)
                throw new IllegalArgumentException("Each attempt must have a saved positive ID.");
            if (!seen.add(record.getId()))
                throw new IllegalArgumentException("Duplicate attempt ID: " + record.getId());
            if (lessonId == 0) lessonId = record.getLessonId();
            if (lessonId != record.getLessonId())
                throw new IllegalArgumentException("Select attempts for the same lesson.");
            ids.add(record.getId());
            elapsed = elapsed.add(record.getElapsedMinutes());
            for (String line : record.getMemberEffort().split("\\R")) {
                // Names can contain colons; LessonExperience validates the final separator.
                int separator = line.lastIndexOf(':');
                effort = effort.add(LessonExperience.parseMinutes("Member effort",
                        line.substring(separator + 1), true));
            }
        }
        BigDecimal mean = elapsed.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);
        // Aggregates may exceed the maximum allowed for one source field.
        return new ExperienceStatistics(lessonId, mean, effort.setScale(2), ids);
    }

    /** Returns the common lesson.
     * @return the lesson shared by all selected attempts
     */
    public int getLessonId() { return lessonId; }
    /** Returns the number of selected attempts.
     * @return number of distinct selected attempts
     */
    public int getAttemptCount() { return sourceIds.size(); }
    /** Returns the rounded mean elapsed time.
     * @return average elapsed minutes, rounded to two places using HALF_UP
     */
    public BigDecimal getMeanElapsedMinutes() { return meanElapsedMinutes; }
    /** Returns the total working effort.
     * @return total member effort in person-minutes, with two decimal places
     */
    public BigDecimal getTotalEffortMinutes() { return totalEffortMinutes; }
    /** Returns the selected source identities.
     * @return independent, read-only source IDs in selection order
     */
    public List<Long> getSourceIds() { return sourceIds; }
}
