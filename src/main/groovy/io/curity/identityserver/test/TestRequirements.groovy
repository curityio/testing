package io.curity.identityserver.test

import java.util.concurrent.TimeUnit

/**
 * Requirments or preconditions that must be met before a test will run.
 *
 * This class provides helpful utilities for checking if the prerequisite of a test are met before they are started.
 * This is useful in Geb browser tests, for example, which might use them in code like this:
 *
 * <pre>
 * import static io.curity.identityserver.test.TestRequirements.isEnvironmentVariableSet
 * import static io.curity.identityserver.test.TestRequirements.isIdshAvailable
 * 
 * {@literal @}Requires( { isIdshAvailable && isEnvironmentVariableSet("SALESFORCE_CLIENT_SECRET") })
 * class SomePluginIT extends GebReportingSpec {
 *     ...
 * }
 * </pre>
 */
class TestRequirements {
    protected TestRequirements() { }

    /**
     * Checks that an environment variable is set.
     *
     * @param name the name of the environment variable
     * @return true if the environment variable with the given name is defined; false otherwise.
     */
    static boolean isEnvironmentVariableSet(String name) {
        def secret = System.getenv(name)
        def isSet = secret != null && !secret.isEmpty()

        if (!isSet) {
            System.err.println("$name environment variable is not set. " +
                    "Set it and retry the test")
        }

        return isSet
    }

    /**
     * Checks that the idsh command is available in the system path and can be executed.
     *
     * This test checks for the presense of the idsh command. It actually invokes it to be certain that it is functional
     * and can connect to the admin node. This will fail if the admin node is down. Consequently, this precondition
     * all checks that the Identity Server's admin node is started on the same machine and can accept connections.
     *
     * @return true if the idsh command is available and contact with the admin node can be established; false otherwise.
     */
    static boolean getIsIdshAvailable() {
        def idsh = "idsh -s".execute()
        idsh.out << "quit\n"
        def isAvailable = idsh.waitFor(2, TimeUnit.SECONDS) && idsh.exitValue() == 0

        if (!isAvailable) {
            System.err.println("idsh is not available in the system PATH. Add it and retry the test")
        }

        return isAvailable
    }
}
