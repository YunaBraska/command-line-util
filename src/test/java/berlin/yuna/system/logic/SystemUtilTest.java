package berlin.yuna.system.logic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.regex.Pattern;

import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.*;
import static java.nio.file.attribute.PosixFilePermission.*;
import static org.hamcrest.CoreMatchers.*;
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
        assertThat(SystemUtil.getOsType(), is(SystemUtil.OperatingSystem.ARM));
    }

    @Test
    public void getOsType_withLinux_shouldReturnLinux() {
        System.setProperty("os.name", "linux");
        assertThat(SystemUtil.getOsType(), is(LINUX));
    }

    @Test
    public void getOsType_withUnix_shouldReturnLinux() {
        System.setProperty("os.name", "unix");
        assertThat(SystemUtil.getOsType(), is(LINUX));
    }

    @Test
    public void getOsType_withMac_shouldReturnMac() {
        System.setProperty("os.name", "mac");
        assertThat(SystemUtil.getOsType(), is(SystemUtil.OperatingSystem.MAC));
    }

    @Test
    public void getOsType_withSonus_shouldReturnSolaris() {
        System.setProperty("os.name", "sunos");
        assertThat(SystemUtil.getOsType(), is(SOLARIS));
    }

    @Test
    public void getOsType_withAix_shouldReturnLinux() {
        System.setProperty("os.name", "ibm-aix");
        assertThat(SystemUtil.getOsType(), is(LINUX));
    }

    @Test
    public void getOsType_withWindows_shouldReturnWindows() {
        System.setProperty("os.name", "MsDos Windows 3.1");
        assertThat(SystemUtil.getOsType(), is(SystemUtil.OperatingSystem.WINDOWS));
    }

    @Test
    public void getOsType_withOtherOS_shouldReturnUnknown() {
        System.setProperty("os.name", "otherOth");
        assertThat(SystemUtil.getOsType(), is(UNKNOWN));
    }

    @Test
    public void killProcessByName_withAnyOsType_shouldExecuteWithoutError(){
        SystemUtil.killProcessByName("testProcess");
    }

    @Test
    public void getKillCommand_shouldReturnRightCommand(){
        assertThat(SystemUtil.getKillCommand(WINDOWS), is(equalTo("taskkill /F /IM")));
        assertThat(SystemUtil.getKillCommand(ARM), is(equalTo("pkill -f")));
        assertThat(SystemUtil.getKillCommand(MAC), is(equalTo("pkill -f")));
        assertThat(SystemUtil.getKillCommand(LINUX), is(equalTo("pkill -f")));
        assertThat(SystemUtil.getKillCommand(SOLARIS), is(equalTo("killall")));
        assertThat(SystemUtil.getKillCommand(UNKNOWN), is(equalTo("killall")));
    }

    @Test
    public void copyResourceFile_shouldBeSuccessful() {
        Path outputPath = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        assertThat(outputPath, is(notNullValue()));
    }

    @Test
    public void copyResourceFile_WithExistingFile_shouldBeSuccessful() {
        Path outputPathFirst = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        Path outputPathSecond = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
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
        Path input = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
        assertThat(SystemUtil.setFilePermissions(input, OWNER_READ), is(true));
        assertThat(SystemUtil.setFilePermissions(input, OWNER_WRITE), is(true));
        assertThat(SystemUtil.setFilePermissions(input, OWNER_EXECUTE), is(true));
    }

    @Test
    public void fixFilePermissions_WithoutPosixPermissions_shouldNotThrowException() throws Exception {
        Path path = SystemUtil.copyResourceToTemp(getClass(), testFileOrigin);
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
    public void getMainResource_shouldBeSuccessful() {
        Path mainResource = SystemUtil.getMainResource(getClass());
        assertThat(mainResource.toFile().exists(), is(true));
    }

    @Test
    public void getTestResource_shouldBeSuccessful() {
        Path testResource = SystemUtil.getTestResource(getClass());
        assertThat(testResource.toFile().exists(), is(true));
    }

    @Test
    public void getTestTargetResource_shouldBeSuccessful() {
        Path targetTestResource = SystemUtil.getTargetTestResource();
        assertThat(targetTestResource.toFile().exists(), is(true));
    }

    @Test
    public void readFile_shouldBeSuccessful() throws URISyntaxException {
        String testResource = SystemUtil.readFile(Paths.get(getClass().getClassLoader().getResource(testFileOrigin).toURI()));
        assertThat(testResource, is(notNullValue()));
    }

    @Test(expected = Exception.class)
    public void readFile_WithNullablePath_shouldThrowException() {
        SystemUtil.readFile(null);
    }

    @Test
    public void readFileLines_shouldBeSuccessful() throws URISyntaxException {
        List<String> testResource = SystemUtil.readFileLines(Paths.get(getClass().getClassLoader().getResource(".gitignore").toURI()));
        assertThat(testResource, is(notNullValue()));
        assertThat(testResource.size(), is(17));
    }

    @Test(expected = Exception.class)
    public void readFileLines_WithNullablePath_shouldThrowException() {
        SystemUtil.readFileLines(null);
    }
}