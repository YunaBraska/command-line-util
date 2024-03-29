package berlin.yuna.clu.logic;

import berlin.yuna.clu.logic.helper.TestMaps;
import berlin.yuna.clu.model.OsArch;
import berlin.yuna.clu.model.OsArchType;
import berlin.yuna.clu.model.OsType;
import berlin.yuna.clu.model.exception.FileCopyException;
import berlin.yuna.clu.model.exception.TerminalExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static berlin.yuna.clu.logic.helper.TestMaps.ARCH_TEST_MAP;
import static berlin.yuna.clu.logic.helper.TestMaps.OS_TEST_MAP;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
class SystemUtilTest {

    private static final String osName = System.getProperty("os.name");
    private static final String osArch = System.getProperty("os.arch");
    private static final String javaTmpDir = System.getProperty("java.io.tmpdir");

    private final String testFileOrigin = "banner.png";
    private final File testFileCopy = new File(System.getProperty("java.io.tmpdir"), testFileOrigin);


    @BeforeEach
    void setUp() {
        SystemUtil.deleteDirectory(testFileCopy.toPath());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("os.name", osName);
        System.setProperty("os.arch", osArch);
        System.setProperty("java.io.tmpdir", javaTmpDir);
    }

    @Test
    void testOsTypes() {
        for (Map.Entry<String, OsType> test : OS_TEST_MAP.entrySet()) {
            assertThat(OsType.of(test.getKey()), is(test.getValue()));
        }
        assertThat(OsType.of("xxx"), is(OsType.OS_UNKNOWN));
    }

    @Test
    void testOsArch() {
        for (Map.Entry<String, TestMaps.ExpectedArch> test : ARCH_TEST_MAP.entrySet()) {
            assertThat("Input was " + test.getKey(), OsArch.of(test.getKey()), is(test.getValue().getOsArch()));
            assertThat("Input was " + test.getKey(), OsArchType.of(test.getKey()), is(test.getValue().getOsArchType()));
        }
    }

    @Test
    void testUnix() {
        for (OsType os : OsType.values()) {
            final boolean isUnix = (isOneOf(os,
                    OsType.OS_AIX,
                    OsType.OS_HP_UX,
                    OsType.OS_IRIX,
                    OsType.OS_LINUX,
                    OsType.OS_DARWIN,
                    OsType.OS_SUN,
                    OsType.OS_SOLARIS,
                    OsType.OS_FREE_BSD,
                    OsType.OS_OPEN_BSD,
                    OsType.OS_NET_BSD
            ));
            assertThat(os.isUnix(), is(isUnix));
        }
    }

    @Test
    void killProcessByName_withAnyOsType_shouldExecuteWithoutError() {
        SystemUtil.killProcessByName("testProcess");
    }

    @Test
    void getKillCommand_shouldReturnRightCommand() {
        assertThat(SystemUtil.killCommand(OsType.OS_WINDOWS), is(equalTo("taskkill /F /IM")));
        assertThat(SystemUtil.killCommand(OsType.OS_DARWIN), is(equalTo("pkill -f")));
        assertThat(SystemUtil.killCommand(OsType.OS_LINUX), is(equalTo("pkill -f")));
        assertThat(SystemUtil.killCommand(OsType.OS_SOLARIS), is(equalTo("killall")));
        assertThat(SystemUtil.killCommand(OsType.OS_UNKNOWN), is(equalTo("killall")));
    }

    @Test
    void copyResourceFile_shouldBeSuccessful() {
        final Path outputPath = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        assertThat(outputPath, is(notNullValue()));
    }

    @Test
    void copyResourceFile_WithExistingFile_shouldBeSuccessful() {
        final Path outputPathFirst = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        final Path outputPathSecond = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        assertThat(outputPathFirst, is(notNullValue()));
        assertThat(outputPathSecond, is(notNullValue()));
        assertThat(outputPathSecond, is(outputPathFirst));
    }

    @Test
    void copyResourceFile_WithOutExistingSourceFile_shouldFailWithNullPointerException() {
        assertThrows(NullPointerException.class, () -> SystemUtil.copyResourceToTemp(getClass(), null));
    }

    @Test
    void fixFilePermissions_shouldBeSuccessful() {
        final Path input = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        assertThat(SystemUtil.setFilePermissions(input, OWNER_READ, OWNER_WRITE, OWNER_EXECUTE), is(true));
        assertThat(SystemUtil.setFilePermissions(input, OWNER_WRITE), is(true));
        assertThat(SystemUtil.setFilePermissions(input, OWNER_EXECUTE), is(true));
    }

    @Test
    void fixFilePermissions_WithoutPosixPermissions_shouldNotThrowException() throws Exception {
        final Path path = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        Files.deleteIfExists(path);
        assertThat(path, is(notNullValue()));

        SystemUtil.setFilePermissions(path);
    }

    @Test
    void fixFilePermissions_onRootFolder_shouldFailAndReturnFalse() {
        assertThat(SystemUtil.setFilePermissions(Paths.get("/"), OTHERS_WRITE), is(false));
    }

    @Test
    void newInstance_smokeTest() {
        assertThat(new TerminalExecutionException("Test") instanceof RuntimeException, is(true));
        assertThat(new FileCopyException("Test", new RuntimeException()) instanceof RuntimeException, is(true));
    }

    @Test
    void readFile_shouldBeSuccessful() throws URISyntaxException {
        final String testResource = SystemUtil.readFile(Paths.get(requireNonNull(getClass().getClassLoader().getResource(testFileOrigin)).toURI()));
        assertThat(testResource, is(notNullValue()));
    }

    @Test
    void readFile_WithNullablePath_shouldThrowException() {
        assertThrows(Exception.class, () -> SystemUtil.readFile(null));
    }

    @Test
    void readFileLines_shouldBeSuccessful() throws URISyntaxException {
        final List<String> testResource = SystemUtil.readFileLines(Paths.get(requireNonNull(getClass().getClassLoader().getResource(
                ".gitignore")).toURI()));
        assertThat(testResource, is(notNullValue()));
        assertThat(testResource.size(), is(17));
    }

    @Test
    void readFileLines_WithNullablePath_shouldThrowException() {
        assertThrows(Exception.class, () -> SystemUtil.readFileLines(null));
    }

    private boolean isOneOf(final OsType os, final OsType... osTypes) {
        for (OsType type : osTypes) {
            if (os == type) {
                return true;
            }
        }
        return false;
    }
}
