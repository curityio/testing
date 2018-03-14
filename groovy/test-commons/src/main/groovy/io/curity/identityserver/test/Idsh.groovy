package io.curity.identityserver.test

import java.util.concurrent.TimeUnit

import static com.google.common.base.Preconditions.checkState

/**
 * A class the abstracts the Identity Shell (idsh) that is provided with the Curity Identity Server.
 *
 * When using this with Spock and Geb, the object will typically be created as a shared instance that all tests can
 * reuse, like in the following example:
 *
 * <pre>
 * {@literal @}Shared
 * Idsh idsh = new Idsh()
 * </pre>
 *
 * When te test is setup, this object will often be used to load a configuration set from an embedded XML resource,
 * like this:
 *
 * <pre>
 * def setupSpec() {
 *     idsh.loadTestConfig("/test-config.xml")
 * }
 * </pre>
 *
 * If extra settings must be made (e.g., to add a secret or ID obtained from the environment in which the test
 * executes), these can be provided when loading configuration, like this:
 *
 * <pre>
 * def setupSpec() {
 *     idsh.loadTestConfig("/test-config.xml", """
 *         set foo $foo
 *         edit zort
 *         set baz
 *         exit
 *         set foobar true
 *     """)
 * }
 * </pre>
 *
 * The commands that can be provided are those accepted by the idsh command interpreter.
 *
 * If a certain test needs to make changes in addition to any done during test setup, it may use the same instance. For
 * example, using Spock, a test may look like this:
 *
 * <pre>
 * def "some test"() {
 *     setup:
 *     idsh.setValue("foo", 22)
 * }
 * </pre>
 */
final class Idsh {
    Idsh() { }

    private int rollbackCount = -1

    /**
     * Loads a configuration file from a resource located in the class path.
     *
     * The loading of the given configuration is doing using a <i>merge</i>, meaning that any loaded configuration
     * will remain. If the resource does not contain a valid set of configuration, after merging with the running
     * configuration, then additional commands can be provided to update the configuration further.
     *
     * The resource should be in XML format; JSON or any other format is not supported. If the XML was obtained from
     * the REST API, the root node of the XML should be renamed, as described in the product documentation.
     *
     * @param resourceFileName the name of the resource file
     * @param extraCommands any extra commands (e.g., a set or delete) that should be applied in the same transaction
     */
    void loadTestConfig(String resourceFileName, String extraCommands = "") {
        def tempFile = copyTestConfigToTempFile(resourceFileName)
        def idsh = "idsh -s".execute()
        idsh.out << """
                configure
                load merge $tempFile                                         
                $extraCommands
                commit
                quit
                quit
            """

        waitForIdshToQuit(idsh)

        def exitValue = idsh.exitValue()

        if (exitValue != 0) {
            System.err.println(idsh.text)
        }

        com.google.common.base.Preconditions.checkState(exitValue == 0, "Idsh exited with a non-zero exit status of $exitValue")
        rollbackCount++
    }

    /**
     * Sets the value of a configuration setting with the given path.
     *
     * @param path the path to the configuration setting to be updated
     * @param the new value (which will have its toString method called to obtain the actual value that is used)
     */
    void setValue(String path, Object value) {
        def idsh = "idsh -s".execute()
        idsh.out << """
            configure
            set $path $value
            commit
            quit
            quit
        """

        waitForIdshToQuit(idsh)
        rollbackCount++
    }

    /**
     * Reverts all changes made since this object was instantiated.
     *
     * This object tracks the number of changes that are made to the running configuration and will revert them all
     * when this method is called. If no changes have been made, then this method will have no effect. This is often
     * called in a cleanupSpec method (or equivalent).
     */
    void rollback() {
        if (rollbackCount < 0) {
            return
        }

        def idsh = "idsh -s".execute()
        idsh << """
                configure
                rollback $rollbackCount
                commit
                quit
                quit
            """

        waitForIdshToQuit(idsh)
        rollbackCount = -1
    }

    private static void waitForIdshToQuit(Process idsh) {
        com.google.common.base.Preconditions.checkState(idsh.waitFor(2, TimeUnit.SECONDS), "Idsh didn't shutdown in a timely manner")
    }

    private static String copyTestConfigToTempFile(String resourceFileName) {
        def temp = File.createTempFile("temp", ".scrap");
        temp.deleteOnExit()
        temp << getClass().getResourceAsStream(resourceFileName)
        temp.absolutePath
    }
}
