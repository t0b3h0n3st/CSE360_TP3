package testbeds;

import java.sql.SQLException;

import database.Database;
import entityClasses.User;

/**
 * Testbed for role authorization used by the Multiple Role Dispatch feature.
 * The tests check whether the database returns the correct roles for each user.
 */
public class MultipleRoleDispatchTestbed {

    private static Database database;

    public static void main(String[] args) throws SQLException {

        // Use a separate in-memory database for testing
        System.setProperty(
            "foundation.db.url",
            "jdbc:h2:mem:role_test;DB_CLOSE_DELAY=-1"
        );

        database = new Database();
        database.connectToDatabase();

        System.out.println("--------------------------------");
        System.out.println("Multiple Role Dispatch Testbed");
        System.out.println("--------------------------------");

        // Create users with different role combinations
        User adminUser = new User(
            "adminTest", "Password1!",
            "Admin", "", "Test", "Admin",
            "admin@test.com",
            true, false, false
        );

        User contributorUser = new User(
            "contributorTest", "Password1!",
            "Contributor", "", "Test", "Contributor",
            "contributor@test.com",
            false, true, false
        );

        User curatorUser = new User(
            "curatorTest", "Password1!",
            "Curator", "", "Test", "Curator",
            "curator@test.com",
            false, false, true
        );

        User multipleRoleUser = new User(
            "multipleTest", "Password1!",
            "Multiple", "", "Test", "Multiple",
            "multiple@test.com",
            true, true, true
        );

        // Add test users to the database
        database.register(adminUser);
        database.register(contributorUser);
        database.register(curatorUser);
        database.register(multipleRoleUser);

        // Run authorization tests
        testRoles("Test 1 - Admin-only user",
                "adminTest",
                true, false, false);

        testRoles("Test 2 - Contributor-only user",
                "contributorTest",
                false, true, false);

        testRoles("Test 3 - Curator-only user",
                "curatorTest",
                false, false, true);

        testRoles("Test 4 - Multiple-role user",
                "multipleTest",
                true, true, true);

        testUnknownUser();

        System.out.println("--------------------------------");
        System.out.println("Testing Complete");
        System.out.println("--------------------------------");
    }

    /**
     * Checks whether the roles returned from the database match
     * the roles expected for the selected user.
     */
    private static void testRoles(
            String testName,
            String username,
            boolean expectedAdmin,
            boolean expectedContributor,
            boolean expectedCurator) {

        System.out.println("\n" + testName);

        boolean userFound = database.getUserAccountDetails(username);

        boolean actualAdmin = database.getCurrentAdminRole();
        boolean actualContributor = database.getCurrentContributorRole();
        boolean actualCurator = database.getCurrentCuratorRole();

        boolean passed =
                userFound &&
                actualAdmin == expectedAdmin &&
                actualContributor == expectedContributor &&
                actualCurator == expectedCurator;

        System.out.println("User found: " + userFound);
        System.out.println("Admin role: " + actualAdmin);
        System.out.println("Contributor role: " + actualContributor);
        System.out.println("Curator role: " + actualCurator);

        if (passed) {
            System.out.println("RESULT: PASS");
        } else {
            System.out.println("RESULT: FAIL");
        }
    }

    /**
     * Checks that a username not stored in the database
     * does not receive any authorized role.
     */
    private static void testUnknownUser() {

        System.out.println("\nTest 5 - Unknown user");

        boolean userFound =
                database.getUserAccountDetails("userDoesNotExist");

        boolean admin = database.getCurrentAdminRole();
        boolean contributor = database.getCurrentContributorRole();
        boolean curator = database.getCurrentCuratorRole();

        boolean passed =
                !userFound &&
                !admin &&
                !contributor &&
                !curator;

        System.out.println("User found: " + userFound);
        System.out.println("Admin role: " + admin);
        System.out.println("Contributor role: " + contributor);
        System.out.println("Curator role: " + curator);

        if (passed) {
            System.out.println("RESULT: PASS");
        } else {
            System.out.println("RESULT: FAIL");
        }
    }
}