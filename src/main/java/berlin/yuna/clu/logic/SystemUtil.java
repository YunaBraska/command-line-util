package berlin.yuna.clu.logic;


import berlin.yuna.clu.model.OsArch;
import berlin.yuna.clu.model.OsArchType;
import berlin.yuna.clu.model.OsType;
import berlin.yuna.clu.model.ThrowingFunction;
import berlin.yuna.clu.model.exception.FileCopyException;
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

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.util.Arrays.asList;

@SuppressWarnings("unused")
public class SystemUtil {

    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    public static final OsType OS = OsType.of(System.getProperty("os.name"));
    public static final OsArch OS_ARCH = OsArch.of(System.getProperty("os.arch"));
    public static final OsArchType OS_ARCH_TYPE = OsArchType.of(System.getProperty("os.arch"));
    public static final boolean IS_UNIX = OS.isOneOf(
            OsType.OS_AIX,
            OsType.OS_HP_UX,
            OsType.OS_IRIX,
            OsType.OS_LINUX,
            OsType.OS_MAC,
            OsType.OS_SUN,
            OsType.OS_SOLARIS,
            OsType.OS_FREE_BSD,
            OsType.OS_OPEN_BSD,
            OsType.OS_NET_BSD
    );

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
                throw new FileCopyException("Could not copy file", e);
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

        try (final Stream<Path> stream = Files.walk(path)) {
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
        new Terminal().execute(getKillCommand(OS) + " " + name);
    }

    static String getKillCommand(final OsType os) {
        switch (os) {
            case OS_WINDOWS:
                return "taskkill /F /IM";
            case OS_SOLARIS:
            case OS_UNKNOWN:
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
