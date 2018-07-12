package berlin.yuna.system.logic;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Paths;

import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.LINUX;
import static berlin.yuna.system.logic.SystemUtil.OperatingSystem.WINDOWS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TerminalTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Terminal terminal;

    @Before
    public void setUp() {
        terminal = new Terminal();
    }

    @Test
    public void getCommandByOsType_withUnix_shouldBuildCorrectly() {
        String[] command = terminal.addExecutor(LINUX, "ls");

        assertThat(command, is(notNullValue()));
        assertThat(command, is(new String[]{"sh", "-c", "ls"}));
    }

    @Test
    public void getCommandByOsType_withWindows_shouldBuildCorrectly() {
        String[] command = terminal.addExecutor(WINDOWS, "dir");

        assertThat(command, is(notNullValue()));
        assertThat(command, is(new String[]{"cmd.exe", "/c", "dir"}));
    }

    @Test
    public void execute_shouldPrintConsoleInfoOutput() {
        assertThat(terminal.process(), is(nullValue()));
        terminal.execute("echo \"Howdy\"");
        assertThat(terminal.process(), is(notNullValue()));
        assertThat(terminal.consoleInfo().toString(), equalTo("Howdy"));
        assertThat(terminal.consoleInfo().length(), is(5));
        assertThat(terminal.consoleError().length(), is(0));
    }

    @Test
    public void clearLogs_shouldClearInfoAndErrorOutput() {
        terminal.execute("echo \"Howdy\"");
        terminal.breakOnError(false).execute("invalidCommand");
        assertThat(terminal.consoleInfo().length(), is(5));
        assertThat(terminal.consoleError().length(), is(37));

        terminal.clearConsole();
        assertThat(terminal.consoleInfo().length(), is(0));
        assertThat(terminal.consoleError().length(), is(0));
    }

    @Test
    public void execute_withWrongCommandAndNoBreakOnError_shouldPrintConsoleErrorOutput() {
        terminal.breakOnError(false).execute("invalidCommand");
        assertThat(terminal.consoleError().toString(), equalTo("sh: invalidCommand: command not found"));
        assertThat(terminal.consoleInfo().length(), is(0));
    }

    @Test
    public void execute_withWrongCommand_shouldThrowException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("sh: invalidCommand: command not found");
        terminal.execute("invalidCommand");
    }

    @Test
    public void execute_inWrongDirectory_shouldThrowIOException() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Cannot run program \"sh\" (in directory \"\"): error=2, No such file or directory");
        terminal.dir("").execute("invalidCommand");
    }

    @Test
    public void settingTimeout_ShouldBeSuccessful() {
        assertThat(terminal.timeoutMs(), is(10000L));
        assertThat(terminal.timeoutMs(69).timeoutMs(), is(69L));
    }

    @Test
    public void settingBreakOnError_ShouldBeSuccessful() {
        assertThat(terminal.breakOnError(), is(true));
        assertThat(terminal.breakOnError(false).breakOnError(), is(false));
    }

    @Test
    public void settingDir_ShouldBeSuccessful() {
        assertThat(terminal.dir().toString(), is(equalTo(System.getProperty("user.dir"))));
        assertThat(terminal.dir("10").dir().toString(), is(equalTo("10")));
        assertThat(terminal.dir(new File("1010")).dir().toString(), is(equalTo("1010")));
        assertThat(terminal.dir(Paths.get("101010")).dir().toString(), is(equalTo("101010")));
    }

    @Test
    public void addConsumerInfo_ShouldBeSuccessful() {
        assertThat(terminal.consumerInfo(System.out::println), is(notNullValue()));
        assertThat(terminal.consumerError(System.err::println), is(notNullValue()));
        terminal.execute("echo \"Howdy\"");
        terminal.breakOnError(false).execute("invalidCommand");
    }

}
