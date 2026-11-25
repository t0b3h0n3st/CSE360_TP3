package entityClasses;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the reply statistics for the discussion board.
 * It counts different students and checks the reply requirement.
 */
public class DiscussionReplyStatistics {

    /**
     * Counts how many different students are in the reply list.
     *
     * @param replies list of students that the user replied to
     * @return number of different students
     */
    public static int countDifferentStudents(List<String> replies) {

        // HashSet keeps each student only once
        Set<String> differentStudents = new HashSet<String>();

        for (String student : replies) {
            differentStudents.add(student);
        }

        return differentStudents.size();
    }

    /**
     * Checks if the user replied to at least three different students.
     *
     * @param replies list of students that the user replied to
     * @return true if the requirement is met, otherwise false
     */
    public static boolean meetsRequirement(List<String> replies) {
        int differentStudents = countDifferentStudents(replies);

        // The participation requirement is met after replying
        // to at least three different students.
        return differentStudents >= 3;
    }
}