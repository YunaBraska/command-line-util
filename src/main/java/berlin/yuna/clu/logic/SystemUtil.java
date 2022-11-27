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
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.Charset.defaultCharset;
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

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class SystemUtil {

    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    public static final OsType OS = OsType.of(System.getProperty("os.name"));
    public static final OsArch OS_ARCH = OsArch.of(System.getProperty("os.arch"));
    public static final OsArchType OS_ARCH_TYPE = OsArchType.of(System.getProperty("os.arch"));

    /**
     * Sets silent file permissions (PosixFilePermissions will be mapped to filePermissions as windows doesn't understand posix)
     *
     * @param path        Path to set permissions on
     * @param permissions permission list to set for the given Path
     * @return true if no error occurred and if permissions are set successfully
     */
    public static boolean setFilePermissions(final Path path, final PosixFilePermission... permissions) {
        final var destination = path.toFile();
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
        final var tmpFile = new File(TMP_DIR, new File(relativePath).getName());
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
            return tryCharsets(charset -> Files.readString(path, charset));
        } catch (Exception e) {
            throw new FileNotReadableException("Could not read file [" + path + "]", e);
        }
    }

    private static <T> T tryCharsets(final ThrowingFunction<Charset, T> function) throws Exception {
        final var latestException = new AtomicReference<Exception>(null);
        for (Charset charset : asList(defaultCharset(), UTF_8, UTF_16, UTF_16BE, UTF_16LE, ISO_8859_1, US_ASCII)) {
            try {
                return function.acceptThrows(charset);
            } catch (Exception e) {
                latestException.set(e);
            } catch (Error e) {
                latestException.set(new MalformedInputException(2));
            }
        }
        throw latestException.get();
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
        final var success = new AtomicBoolean(true);

        try (final var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            success.set(false);
                        }
                    });
        } catch (IOException ex) {
            return false;
        }
        return success.get();
    }

    /**
     * kills processes by name
     *
     * @param name name of the process to kill
     */
    public static void killProcessByName(final String name) {
        new Terminal().execute(killCommand(OS) + " " + name);
    }

    public static String killCommand(final OsType os) {
        return switch (os) {
            case OS_WINDOWS -> "taskkill /F /IM";
            case OS_SOLARIS, OS_UNKNOWN -> "killall";
            default -> "pkill -f";
        };
    }

    private SystemUtil() {
    }

    private static boolean setFilePermission(final File destination, final PosixFilePermission permission) {
        return switch (permission) {
            case OWNER_WRITE, GROUP_WRITE, OTHERS_WRITE -> destination.setWritable(true, permission == OWNER_WRITE);
            case OWNER_READ, GROUP_READ, OTHERS_READ -> destination.setReadable(true, permission == OWNER_READ);
            case OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE ->
                    destination.setExecutable(true, permission == OWNER_EXECUTE);
        };
    }
}
