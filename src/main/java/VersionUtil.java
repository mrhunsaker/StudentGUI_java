import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to retrieve the version of the application from a properties file.
 * <p>
 * The version is read from a properties file named {@code version.properties} located in the root of the classpath.
 * If the file is not found or an I/O error occurs, the version is set to "unknown".
 * </p>
 */
public class VersionUtil {

    /** The path to the properties file containing the version information. */
    private static final String VERSION_FILE = "/version.properties";

    /** The version of the application, initialized from the properties file. */
    private static String version;

    /**
     * Static block to initialize the {@link #version} variable.
     * <p>
     * The static block loads the version from the {@code version.properties} file.
     * If the file cannot be found or an I/O error occurs, the version is set to "unknown".
     * </p>
     */
    static {
        try (InputStream input = VersionUtil.class.getResourceAsStream(VERSION_FILE)) {
            Properties properties = new Properties();
            if (input == null) {
                // If the properties file is not found, set version to "unknown"
                System.err.println("Unable to find " + VERSION_FILE);
                version = "unknown";
            } else {
                // Load the properties file and set the version
                properties.load(input);
                version = properties.getProperty("version", "unknown");
            }
        } catch (IOException ex) {
            // Print the stack trace and set version to "unknown" in case of an exception
            ex.printStackTrace();
            version = "unknown";
        }
    }

    /**
     * Returns the version of the application.
     * <p>
     * This method provides access to the version information that was loaded from the properties file.
     * If the properties file could not be found or an error occurred, it returns "unknown".
     * </p>
     *
     * @return The version of the application.
     */
    public static String getVersion() {
        return version;
    }
}
