package berlin.yuna.clu.logic;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Paths;

import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.LINUX;
import static berlin.yuna.clu.logic.SystemUtil.OperatingSystem.WINDOWS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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
        final String[] command = terminal.addExecutor(LINUX, "ls");

        assertThat(command, is(notNullValue()));
        assertThat(command, is(new String[]{"sh", "-c", "ls"}));
    }

    @Test
    public void getCommandByOsType_withWindows_shouldBuildCorrectly() {
        final String[] command = terminal.addExecutor(WINDOWS, "dir");

        assertThat(command, is(notNullValue()));
        assertThat(command, is(new String[]{"cmd.exe", "/c", "dir"}));
    }

    @Test
    public void execute_shouldPrintConsoleInfoOutput() {
        assertThat(terminal.process(), is(nullValue()));
        terminal.execute("echo Howdy");
        assertThat(terminal.process(), is(notNullValue()));
        assertThat(terminal.consoleInfo(), containsString("Howdy"));
        assertThat(terminal.consoleError().length(), is(0));
        assertThat(terminal.status(), is(0));
    }

    @Test
    public void clearLogs_shouldClearInfoAndErrorOutput() {
        terminal.execute("echo \"Howdy\"");
        terminal.timeoutMs(512).breakOnError(false).execute("invalidCommand");
        assertThat(terminal.consoleInfo(), containsString("Howdy"));
        assertThat(terminal.consoleError(), containsString("not found"));

        terminal.clearConsole();
        assertThat(terminal.consoleInfo().length(), is(0));
        assertThat(terminal.consoleError().length(), is(0));
    }

    @Test
    public void execute_withWrongCommandAndNoBreakOnError_shouldPrintConsoleErrorOutput() {
        terminal.timeoutMs(512).breakOnError(false).execute("invalidCommand");
        assertThat(terminal.consoleError(), containsString("invalidCommand"));
        assertThat(terminal.consoleError(), containsString("not found"));
        assertThat(terminal.consoleInfo().length(), is(0));
    }

    @Test
    public void execute_withWrongCommandAndTimeout_shouldThrowException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("not found");
        terminal.timeoutMs(256).execute("invalidCommand");
    }

    @Test
    public void execute_withWrongCommandAndTimeoutAndBreakOnErrorFalse_shouldThrowException() {
        terminal.timeoutMs(256).breakOnError(false).execute("invalidCommand");
        assertThat(terminal.status(), is(2));
        assertThat(terminal.consoleError(), containsString("invalidCommand"));
        assertThat(terminal.consoleError(), containsString("not found"));
    }

    @Test
    public void execute_inWrongDirectory_shouldThrowIOException() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Cannot run program");
        terminal.dir("").execute("invalidCommand");
    }

    @Test
    public void settingTimeout_ShouldBeSuccessful() {
        assertThat(terminal.timeoutMs(), is(-1L));
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

    @Test
    public void executeTwice_ShouldReturnMessagesFromBothCommands() {
        final String console = terminal.execute("echo \"Sub\"").execute("echo \"ject\"").consoleInfo();
        assertThat(console, containsString("Sub"));
        assertThat(console, containsString("ject"));
    }

    @Test
    public void execute_ShouldContainSystemPropertiesAsWell() {
        System.setProperty("aa", "bb");
        final String console = terminal.timeoutMs(256).execute("echo $aa").consoleInfo();
        assertThat(console, containsString("bb"));
        assertThat(console, not(containsString("aa")));
    }

}
