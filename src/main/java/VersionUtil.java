import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to surface project version information.
 *
 * Reads the {@code /version.properties} file from the classpath and exposes
 * the {@link #getVersion()} helper. If the file cannot be read, returns
 * {@code "unknown"}.
 */
public class VersionUtil {
    private static final Logger LOG = LoggerFactory.getLogger(VersionUtil.class);

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
                    LOG.warn("Unable to find {}", VERSION_FILE);
                    version = "unknown";
                } else {
                // Load the properties file and set the version
                properties.load(input);
                version = properties.getProperty("version", "unknown");
            }
        } catch (IOException ex) {
            // Log the exception and set version to "unknown" in case of an exception
            LOG.error("Error reading version properties", ex);
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

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private VersionUtil() {
        throw new AssertionError("Not instantiable");
    }
}
