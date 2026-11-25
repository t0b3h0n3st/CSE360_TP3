package testbeds;

import java.util.ArrayList;
import java.util.List;

import entityClasses.DiscussionReplyStatistics;

/**
 * Testbed for the discussion reply statistics prototype.
 * Tests different reply situations and the three-student requirement.
 */
public class DiscussionReplyTestbed {

	/**
	 * Runs the test cases for the discussion reply statistics.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {

        System.out.println("--------------------------------");
        System.out.println("Discussion Reply Testbed");
        System.out.println("--------------------------------");

        // Test 1: replies to three different students
        List<String> replies = new ArrayList<String>();
        replies.add("StudentB");
        replies.add("StudentC");
        replies.add("StudentD");

        int expected = 3;

        // Get the number of different students
        int actual = DiscussionReplyStatistics.countDifferentStudents(replies);

        System.out.println("Test: Three different students");
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " + actual);

        if (actual == expected) {
            System.out.println("RESULT: PASS");
        } else {
            System.out.println("RESULT: FAIL");
        }
        
        
        // Test 2: reply to the same student three times
        System.out.println();

        System.out.println("Test: Three replies to the same student");

        List<String> sameStudentReplies = new ArrayList<String>();

        // All three replies are to StudentB
        sameStudentReplies.add("StudentB");
        sameStudentReplies.add("StudentB");
        sameStudentReplies.add("StudentB");

        // StudentB should only be counted once
        int expectedSameStudent = 1;

        int actualSameStudent =
                DiscussionReplyStatistics.countDifferentStudents(sameStudentReplies);

        System.out.println("Expected: " + expectedSameStudent);
        System.out.println("Actual: " + actualSameStudent);

        // Compare the actual result with the expected result
        if (actualSameStudent == expectedSameStudent) {
            System.out.println("RESULT: PASS");
        } else {
            System.out.println("RESULT: FAIL");
        }
        
        
        // Test 3: no replies
        System.out.println();

        System.out.println("Test: No replies");

        // Create an empty reply list
        List<String> noReplies = new ArrayList<String>();

        // No replies means no different students
        int expectedNoReplies = 0;

        int actualNoReplies =
                DiscussionReplyStatistics.countDifferentStudents(noReplies);

        System.out.println("Expected: " + expectedNoReplies);
        System.out.println("Actual: " + actualNoReplies);

        // Check the result
        if (actualNoReplies == expectedNoReplies) {
            System.out.println("RESULT: PASS");
        } else {
            System.out.println("RESULT: FAIL");
        }
        
        
        // Test 4: check the three student requirement
        System.out.println();

        System.out.println("Test: Participation requirement");

        // Reply to only two different students
        List<String> twoStudents = new ArrayList<String>();
        twoStudents.add("StudentB");
        twoStudents.add("StudentC");

        boolean expectedTwo = false;
        boolean actualTwo =
                DiscussionReplyStatistics.meetsRequirement(twoStudents);

        System.out.println("Two different students");
        System.out.println("Expected: " + expectedTwo);
        System.out.println("Actual: " + actualTwo);

        if (actualTwo == expectedTwo) {
            System.out.println("RESULT: PASS");
        } else {
            System.out.println("RESULT: FAIL");
        }

        // Add one more different student
        twoStudents.add("StudentD");

        boolean expectedThree = true;
        boolean actualThree =
                DiscussionReplyStatistics.meetsRequirement(twoStudents);

        System.out.println();
        System.out.println("Three different students");
        System.out.println("Expected: " + expectedThree);
        System.out.println("Actual: " + actualThree);

        if (actualThree == expectedThree) {
            System.out.println("RESULT: PASS");
        } else {
            System.out.println("RESULT: FAIL");
        }
    }
}