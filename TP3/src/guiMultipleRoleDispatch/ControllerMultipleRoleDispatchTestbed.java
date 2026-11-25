package guiMultipleRoleDispatch;

/**
 * Testbed for the role-dispatch decision logic used by
 * ControllerMultipleRoleDispatch.performRole().
 *
 * The tests cover Admin, Contributor, Curator,
 * and an unsupported role.
 */
public class ControllerMultipleRoleDispatchTestbed {

    public static void main(String[] args) {

        System.out.println("--------------------------------");
        System.out.println("Controller Multiple Role Dispatch Testbed");
        System.out.println("--------------------------------");

        testRole("Test 1 - Admin",
                "Admin",
                "ADMIN_HOME");

        testRole("Test 2 - Contributor",
                "Contributor",
                "CONTRIBUTOR_HOME");

        testRole("Test 3 - Curator",
                "Curator",
                "CURATOR_HOME");

        testRole("Test 4 - Unsupported Role",
                "Viewer",
                "INVALID_ROLE");

        System.out.println("--------------------------------");
        System.out.println("Testing Complete");
        System.out.println("--------------------------------");
    }


    /**
     * Runs one role-dispatch test and compares the
     * actual result with the expected result.
     */
    private static void testRole(
            String testName,
            String role,
            String expectedResult) {

        System.out.println("\n" + testName);

        String actualResult = determineRolePath(role);

        System.out.println("Input role: " + role);
        System.out.println("Expected: " + expectedResult);
        System.out.println("Actual: " + actualResult);

        if (expectedResult.equals(actualResult)) {
            System.out.println("RESULT: PASS");
        } else {
            System.out.println("RESULT: FAIL");
        }
    }


    /**
     * Represents the role-selection decision logic used
     * by ControllerMultipleRoleDispatch.performRole().
     */
    private static String determineRolePath(String role) {

        if (role.compareTo("Admin") == 0) {
            return "ADMIN_HOME";

        } else if (role.compareTo("Contributor") == 0) {
            return "CONTRIBUTOR_HOME";

        } else if (role.compareTo("Curator") == 0) {
            return "CURATOR_HOME";

        } else {
            return "INVALID_ROLE";
        }
    }
}