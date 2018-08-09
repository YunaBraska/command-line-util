package berlin.yuna.system.logic;


import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.ARM;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.LINUX;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.MAC;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.SOLARIS;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.UNKNOWN;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.WINDOWS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;


public class SystemUtil {

    public static final String RESOURCE_TYPE_MAIN = "main";
    public static final String RESOURCE_TYPE_TEST = "test";
    public static final String RESOURCE_TYPE_TARGET_TEST = "target/test-classes";
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String USER_DIR = System.getProperty("user.dir");

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
     * Sets silent file permissions (PosixFilePermissions will be mapped to filePermissions as windows doesn't understand posix)
     * @param path        Path to set permissions on
     * @param permissions permission list to set for the given Path
     * @return true if no error occurred and if permissions are set successfully
     */
    public static boolean setFilePermissions(final Path path, final PosixFilePermission... permissions) {
        File destination = path.toFile();
        for(PosixFilePermission permission : permissions) {
            if(!setFilePermission(destination, permission)){
                return false;
            }
        }
        return true;
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
        return getResource(clazz, RESOURCE_TYPE_MAIN);
    }

    /**
     * Gets resource folder from test sources instead of target sources
     *
     * @param clazz Caller class to find its resource / get its classloader
     * @return test resource path
     */
    public static Path getTestResource(final Class clazz) {
        return getResource(clazz, RESOURCE_TYPE_TEST);
    }

    /**
     * Gets resource folder from target test sources instead of target sources
     *
     * @return target test resource path
     */
    public static Path getTargetTestResource() {
        return Paths.get(USER_DIR, RESOURCE_TYPE_TARGET_TEST);
    }

    public static Path getResourceFolder(final Class clazz) {
        try {
            ClassLoader classLoader = clazz.getClassLoader();
            return Paths.get(requireNonNull(classLoader.getResource("")).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * kills processes by name
     *
     * @param name name of the process to kill
     */
    public static void killProcessByName(final String name) {
        new Terminal().execute(getKillCommand(getOsType()) + " " + name);
    }

    static String getKillCommand(OperatingSystem operatingSystem) {
        switch (operatingSystem) {
            case WINDOWS:
                return "taskkill /F /IM";
            case ARM:
            case MAC:
            case LINUX:
                return "pkill -f";
            case SOLARIS:
            case UNKNOWN:
                return "killall";
        }
        return "pkill -f";
    }

    private static Path getResource(final Class clazz, final String resourceType) {
        String resPath = getResourceFolder(clazz).toString();
        resPath = resPath.replace("target/classes", "src/" + resourceType + "/resources");
        resPath = resPath.replace("target/test-classes", "src/" + resourceType + "/resources");
        return Paths.get(resPath);
    }

    private static boolean setFilePermission(File destination, PosixFilePermission permission) {
        boolean successState = false;
        switch (permission) {
            case OWNER_WRITE:
            case GROUP_WRITE:
            case OTHERS_WRITE:
                successState = destination.setWritable(true, permission == OWNER_WRITE);
                break;
            case OWNER_READ:
            case GROUP_READ:
            case OTHERS_READ:
                successState = destination.setReadable(true, permission == OWNER_READ);
                break;
            case OWNER_EXECUTE:
            case GROUP_EXECUTE:
            case OTHERS_EXECUTE:
                successState = destination.setExecutable(true, permission == OWNER_EXECUTE);
                break;
        }
        return successState;
    }
}
