package berlin.yuna.system.logic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.ARM;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.LINUX;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.MAC;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.SOLARIS;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.UNKNOWN;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.WINDOWS;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SystemUtilTest {

    private static final String osName = System.getProperty("os.name");
    private static final String osArch = System.getProperty("os.arch");
    private static final String javaTmpDir = System.getProperty("java.io.tmpdir");

    private final String testFileOrigin = "banner.png";
    private final File testFileCopy = new File(System.getProperty("java.io.tmpdir"), testFileOrigin);

    @Before
    public void setUp() {
        SystemUtil.deleteDirectory(testFileCopy.toPath());
    }

    @After
    public void tearDown() {
        System.setProperty("os.name", osName);
        System.setProperty("os.arch", osArch);
        System.setProperty("java.io.tmpdir", javaTmpDir);
    }

    @Test
    public void getOsType_withArm_shouldReturnArm() {
        System.setProperty("os.arch", "arm-linux");
        System.setProperty("os.name", "notRelevant");
        assertThat(SystemUtil.getOsType(), is(ARM));
        assertThat(SystemUtil.isArm(), is(true));
    }

    @Test
    public void getOsType_withLinux_shouldReturnLinux() {
        System.setProperty("os.name", "linux");
        assertThat(SystemUtil.getOsType(), is(LINUX));
        assertThat(SystemUtil.isLinux(), is(true));
    }

    @Test
    public void getOsType_withUnix_shouldReturnLinux() {
        System.setProperty("os.name", "unix");
        assertThat(SystemUtil.getOsType(), is(LINUX));
        assertThat(SystemUtil.isLinux(), is(true));
    }

    @Test
    public void getOsType_withMac_shouldReturnMac() {
        System.setProperty("os.name", "mac");
        assertThat(SystemUtil.getOsType(), is(MAC));
        assertThat(SystemUtil.isMac(), is(true));
    }

    @Test
    public void getOsType_withSonus_shouldReturnSolaris() {
        System.setProperty("os.name", "sunos");
        assertThat(SystemUtil.getOsType(), is(SOLARIS));
        assertThat(SystemUtil.isSolaris(), is(true));
    }

    @Test
    public void getOsType_withAix_shouldReturnLinux() {
        System.setProperty("os.name", "ibm-aix");
        assertThat(SystemUtil.getOsType(), is(LINUX));
        assertThat(SystemUtil.isLinux(), is(true));
    }

    @Test
    public void getOsType_withWindows_shouldReturnWindows() {
        System.setProperty("os.name", "MsDos Windows 3.1");
        assertThat(SystemUtil.getOsType(), is(SystemUtil.OperatingSystem.WINDOWS));
        assertThat(SystemUtil.isWindows(), is(true));
    }

    @Test
    public void getOsType_withOtherOS_shouldReturnUnknown() {
        System.setProperty("os.name", "otherOth");
        assertThat(SystemUtil.getOsType(), is(UNKNOWN));
        assertThat(SystemUtil.isUnknown(), is(true));
    }

    @Test
    public void killProcessByName_withAnyOsType_shouldExecuteWithoutError() {
        SystemUtil.killProcessByName("testProcess");
    }

    @Test
    public void getKillCommand_shouldReturnRightCommand() {
        assertThat(SystemUtil.getKillCommand(WINDOWS), is(equalTo("taskkill /F /IM")));
        assertThat(SystemUtil.getKillCommand(ARM), is(equalTo("pkill -f")));
        assertThat(SystemUtil.getKillCommand(MAC), is(equalTo("pkill -f")));
        assertThat(SystemUtil.getKillCommand(LINUX), is(equalTo("pkill -f")));
        assertThat(SystemUtil.getKillCommand(SOLARIS), is(equalTo("killall")));
        assertThat(SystemUtil.getKillCommand(UNKNOWN), is(equalTo("killall")));
    }

    @Test
    public void copyResourceFile_shouldBeSuccessful() {
        final Path outputPath = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        assertThat(outputPath, is(notNullValue()));
    }

    @Test
    public void copyResourceFile_WithExistingFile_shouldBeSuccessful() {
        final Path outputPathFirst = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        final Path outputPathSecond = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        assertThat(outputPathFirst, is(notNullValue()));
        assertThat(outputPathSecond, is(notNullValue()));
        assertThat(outputPathSecond, is(outputPathFirst));
    }

    @Test(expected = NullPointerException.class)
    public void copyResourceFile_WithOutExistingSourceFile_shouldFailWithNullPointerException() {
        SystemUtil.copyResourceToTemp(getClass(), null);
    }

    @Test
    public void fixFilePermissions_shouldBeSuccessful() {
        final Path input = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        assertThat(SystemUtil.setFilePermissions(input, OWNER_READ, OWNER_WRITE, OWNER_EXECUTE), is(true));
        assertThat(SystemUtil.setFilePermissions(input, OWNER_WRITE), is(true));
        assertThat(SystemUtil.setFilePermissions(input, OWNER_EXECUTE), is(true));
    }

    @Test
    public void fixFilePermissions_WithoutPosixPermissions_shouldNotThrowException() throws Exception {
        final Path path = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        Files.deleteIfExists(path);
        assertThat(path, is(notNullValue()));

        SystemUtil.setFilePermissions(path);
    }

    @Test
    public void fixFilePermissions_onRootFolder_shouldFailAndReturnFalse() {
        assertThat(SystemUtil.setFilePermissions(Paths.get("/"), OTHERS_WRITE), is(false));
    }

    @Test
    public void newInstance_smokeTest() {
        assertThat(new SystemUtil(), is(notNullValue()));
    }

    @Test
    public void readFile_shouldBeSuccessful() throws URISyntaxException {
        final String testResource = SystemUtil.readFile(Paths.get(requireNonNull(getClass().getClassLoader().getResource(
                testFileOrigin)).toURI()));
        assertThat(testResource, is(notNullValue()));
    }

    @Test(expected = Exception.class)
    public void readFile_WithNullablePath_shouldThrowException() {
        SystemUtil.readFile(null);
    }

    @Test
    public void readFileLines_shouldBeSuccessful() throws URISyntaxException {
        final List<String> testResource = SystemUtil.readFileLines(Paths.get(requireNonNull(getClass().getClassLoader().getResource(
                ".gitignore")).toURI()));
        assertThat(testResource, is(notNullValue()));
        assertThat(testResource.size(), is(17));
    }

    @Test(expected = Exception.class)
    public void readFileLines_WithNullablePath_shouldThrowException() {
        SystemUtil.readFileLines(null);
    }
}