package berlin.yuna.clu.logic;


import berlin.yuna.clu.model.ThrowingFunction;
import berlin.yuna.clu.model.exception.FileCpoyException;
import berlin.yuna.clu.model.exception.FileNotReadableException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.ARM;
import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.LINUX;
import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.MAC;
import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.SOLARIS;
import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.UNKNOWN;
import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.WINDOWS;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Arrays.asList;

@SuppressWarnings("unused")
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
        final String osName = System.getProperty("os.name").toLowerCase();
        final String osArch = System.getProperty("os.arch").toLowerCase();
        if (osArch.contains("arm")) {
            return ARM;
        } else if ((osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))) {
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
     * Checks operating system with {@link SystemUtil#getOsType()}
     *
     * @return true if the system is {@link OperatingSystem#ARM}
     */
    public static boolean isArm() {
        return getOsType() == ARM;
    }

    /**
     * Checks operating system with {@link SystemUtil#getOsType()}
     *
     * @return true if the system is {@link OperatingSystem#LINUX}
     */
    public static boolean isLinux() {
        return getOsType() == LINUX;
    }

    /**
     * Checks operating system with {@link SystemUtil#getOsType()}
     *
     * @return true if the system is {@link OperatingSystem#MAC}
     */
    public static boolean isMac() {
        return getOsType() == MAC;
    }

    /**
     * Checks operating system with {@link SystemUtil#getOsType()}
     *
     * @return true if the system is {@link OperatingSystem#WINDOWS}
     */
    public static boolean isWindows() {
        return getOsType() == WINDOWS;
    }

    /**
     * Checks operating system with {@link SystemUtil#getOsType()}
     *
     * @return true if the system is {@link OperatingSystem#SOLARIS}
     */
    public static boolean isSolaris() {
        return getOsType() == SOLARIS;
    }

    /**
     * Checks operating system with {@link SystemUtil#getOsType()}
     *
     * @return true if the system is {@link OperatingSystem#UNKNOWN}
     */
    public static boolean isUnknown() {
        return getOsType() == UNKNOWN;
    }

    /**
     * Sets silent file permissions (PosixFilePermissions will be mapped to filePermissions as windows doesn't understand posix)
     *
     * @param path        Path to set permissions on
     * @param permissions permission list to set for the given Path
     * @return true if no error occurred and if permissions are set successfully
     */
    public static boolean setFilePermissions(final Path path, final PosixFilePermission... permissions) {
        final File destination = path.toFile();
        for (PosixFilePermission permission : permissions) {
            if (!setFilePermission(destination, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies a source file to temp path as the resources are not accessible/executable
     *
     * @param clazz        Caller class to find its resource / get its classloader
     * @param relativePath relative resource file path
     * @return temp path from copied file output
     */
    public static Path copyResourceToTemp(final Class<?> clazz, final String relativePath) {
        final File tmpFile = new File(TMP_DIR, new File(relativePath).getName());
        if (!tmpFile.exists()) {
            try {
                Files.copy(Objects.requireNonNull(clazz.getClassLoader().getResourceAsStream(relativePath)), tmpFile.toPath());
            } catch (Exception e) {
                throw new FileCpoyException("Could not copy file", e);
            }
        }
        return tmpFile.toPath();
    }

    /**
     * Reads in a whole file
     *
     * @param path Filepath to read from
     * @return File content
     */
    public static String readFile(final Path path) {
        try {
            return tryCharsets(charset -> new String(Files.readAllBytes(path), charset));
        } catch (Exception e) {
            throw new FileNotReadableException("Could not read file [" + path + "]", e);
        }
    }

    private static <T> T tryCharsets(final ThrowingFunction<Charset, T> function) throws Exception {
        Exception last = null;
        for (Charset charset : asList(UTF_8, UTF_16, UTF_16BE, UTF_16LE, ISO_8859_1, US_ASCII)) {
            try {
                return function.acceptThrows(charset);
            } catch (Exception e) {
                last = e;
            }
        }
        throw last;
    }

    /**
     * Reads in a whole file
     *
     * @param path Filepath to read from
     * @return File content line wise
     */
    public static List<String> readFileLines(final Path path) {
        try {
            return tryCharsets(charset -> Files.readAllLines(path, charset));
        } catch (Exception e) {
            throw new FileNotReadableException("Could not read file [" + path + "]", e);
        }
    }

    /**
     * Deletes silent a directory
     *
     * @param path directory to delete
     * @return false on exception and any deletion error
     */
    public static boolean deleteDirectory(final Path path) {
        final AtomicBoolean success = new AtomicBoolean(true);

        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> success.set((success.get() && file.delete()) && success.get()));
        } catch (IOException ex) {
            return success.get();
        }
        return success.get();
    }

    /**
     * kills processes by name
     *
     * @param name name of the process to kill
     */
    public static void killProcessByName(final String name) {
        new Terminal().execute(getKillCommand(getOsType()) + " " + name);
    }

    static String getKillCommand(final OperatingSystem operatingSystem) {
        switch (operatingSystem) {
            case WINDOWS:
                return "taskkill /F /IM";
            case SOLARIS:
            case UNKNOWN:
                return "killall";
            default:
                return "pkill -f";
        }
    }

    private static boolean setFilePermission(final File destination, final PosixFilePermission permission) {
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
