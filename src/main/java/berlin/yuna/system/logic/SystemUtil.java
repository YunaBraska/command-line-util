package berlin.yuna.system.logic;


import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.ARM;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.LINUX;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.MAC;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.SOLARIS;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.UNKNOWN;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.WINDOWS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class SystemUtil {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");


    /**
     * Supported operation system enum
     */
    public enum OperatingSystem {
        ARM, LINUX, MAC, WINDOWS, SOLARIS, UNKNOWN
    }

    /**
     * Get current operating system
     *
     * @return current {@link OperatingSystem} if supported, else {@link OperatingSystem#UNKNOWN}
     */
    public static OperatingSystem getOsType() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        if (osArch.contains("arm")) {
            return ARM;
        } else if ((osName.contains("nix") || osName.contains("nux") || osName.indexOf("aix") > 0)) {
            return LINUX;
        } else if (osName.contains("mac")) {
            return MAC;
        } else if (osName.contains("win")) {
            return WINDOWS;
        } else if (osName.contains("sunos")) {
            return SOLARIS;
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Sets silent file permissions
     *
     * @param path        Path to set permissions on
     * @param permissions permission list to set for the given Path
     * @return true if no error occurred and if permissions are set successfully
     */
    public static boolean setFilePermissions(Path path, PosixFilePermission... permissions) {
        try {
            Files.setPosixFilePermissions(path, EnumSet.copyOf(asList(permissions)));
            Set<PosixFilePermission> result = Files.getPosixFilePermissions(path, NOFOLLOW_LINKS);

            for (PosixFilePermission permission : permissions) {
                if (!result.contains(permission)) {
                    return false;
                }
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Copies a source file to temp path as the resources are not accessable/executeable
     *
     * @param clazz        Caller class to find its resource / get its classloader
     * @param relativePath relative resource file path
     * @return temp path from copied file output
     */
    public static Path copyResourceToTemp(final Class clazz, final String relativePath) {
        File tmpFile = new File(TMP_DIR, new File(relativePath).getName());
        if (!tmpFile.exists()) {
            try {
                Files.copy(clazz.getClassLoader().getResourceAsStream(relativePath), tmpFile.toPath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return tmpFile.toPath();
    }

    /**
     * Gets resource folder from main sources instead of target sources
     *
     * @param clazz Caller class to find its resource / get its classloader
     * @return main resource path
     */
    public static Path getMainResource(final Class clazz) {
        return getResource(clazz, "main");
    }

    /**
     * Gets resource folder from test sources instead of target sources
     *
     * @param clazz Caller class to find its resource / get its classloader
     * @return test resource path
     */
    public static Path getTestResource(final Class clazz) {
        return getResource(clazz, "test");
    }

    public static String getResourceFolder(final Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        return requireNonNull(classLoader.getResource("")).getPath();
    }

    /**
     * Reads in a whole file
     *
     * @param path Filepath to read from
     * @return File content
     */
    public static String readFile(final Path path) {
        try {
            return new String(Files.readAllBytes(path), UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not read test file cause: \n" + e);
        }
    }

    /**
     * Reads in a whole file
     *
     * @param path Filepath to read from
     * @return File content line wise
     */
    public static List<String> readFileLines(final Path path) {
        try {
            return Files.readAllLines(path, UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not read test file cause: \n" + e);
        }
    }

    /**
     * Deletes silent a directory
     *
     * @param path directory to delete
     * @return true if no exception occurred
     */
    public static boolean deleteDirectory(final Path path) {
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    private static Path getResource(final Class clazz, final String resourceType) {
        String resPath = getResourceFolder(clazz);
        resPath = resPath.replace("target/classes", "src/" + resourceType + "/resources");
        resPath = resPath.replace("target/test-classes", "src/" + resourceType + "/resources");
        return Paths.get(resPath);
    }
}
