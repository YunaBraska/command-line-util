package berlin.yuna.clu.logic;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static berlin.yuna.clu.model.OsType.OS_LINUX;
import static berlin.yuna.clu.model.OsType.OS_WINDOWS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
class TerminalTest {

    private Terminal terminal;

    @BeforeEach
    void setUp() {
        terminal = new Terminal();
    }

    @Test
    void getCommandByOsType_withUnix_shouldBuildCorrectly() {
        final String[] command = terminal.addExecutor(OS_LINUX, "ls");

        assertThat(command, is(notNullValue()));
        assertThat(command, is(new String[]{"sh", "-c", "ls"}));
    }

    @Test
    void getCommandByOsType_withWindows_shouldBuildCorrectly() {
        final String[] command = terminal.addExecutor(OS_WINDOWS, "dir");

        assertThat(command, is(notNullValue()));
        assertThat(command, is(new String[]{"cmd.exe", "/c", "dir"}));
    }

    @Test
    void execute_shouldPrintConsoleInfoOutput() {
        assertThat(terminal.process(), is(nullValue()));
        terminal.execute("echo Howdy");
        assertThat(terminal.process(), is(notNullValue()));
        assertThat(terminal.consoleInfo(), containsString("Howdy"));
        assertThat(terminal.consoleError().length(), is(0));
        assertThat(terminal.status(), is(0));
    }

    @Test
    void clearLogs_shouldClearInfoAndErrorOutput() {
        terminal.execute("echo \"Howdy\"");
        terminal.timeoutMs(512).breakOnError(false).execute("invalidCommand");
        assertThat(terminal.consoleInfo(), containsString("Howdy"));
        assertThat(terminal.consoleError(), containsString("not found"));

        terminal.clearConsole();
        assertThat(terminal.consoleInfo().length(), is(0));
        assertThat(terminal.consoleError().length(), is(0));
    }

    @Test
    void execute_withWrongCommandAndNoBreakOnError_shouldPrintConsoleErrorOutput() {
        terminal.timeoutMs(512).breakOnError(false).execute("invalidCommand");
        assertThat(terminal.consoleError(), containsString("invalidCommand"));
        assertThat(terminal.consoleError(), containsString("not found"));
        assertThat(terminal.consoleInfo().length(), is(0));
    }

    @Test
    void execute_withWrongCommandAndTimeout_shouldThrowException() {
        assertThrows(IllegalStateException.class, () -> terminal.timeoutMs(256).breakOnError(true).execute("invalidCommand"));
    }

    @Test
    void execute_withWrongCommandAndTimeoutAndBreakOnErrorFalse_shouldThrowException() {
        terminal.timeoutMs(256).breakOnError(false).execute("invalidCommand");
        assertThat(terminal.status(), is(not(0)));
        assertThat(terminal.consoleError(), containsString("invalidCommand"));
        assertThat(terminal.consoleError(), containsString("not found"));
    }

    @Test
    void execute_inWrongDirectory_shouldThrowIOException() {
        assertThrows(RuntimeException.class, () -> terminal.dir("").execute("invalidCommand"));
    }

    @Test
    void settingTimeout_ShouldBeSuccessful() {
        assertThat(terminal.timeoutMs(), is(-1L));
        assertThat(terminal.timeoutMs(69).timeoutMs(), is(69L));
    }

    @Test
    void settingBreakOnError_ShouldBeSuccessful() {
        assertThat(terminal.breakOnError(), is(false));
        assertThat(terminal.breakOnError(false).breakOnError(), is(false));
    }

    @Test
    void settingDir_ShouldBeSuccessful() {
        assertThat(terminal.dir().toString(), is(equalTo(System.getProperty("user.dir"))));
        assertThat(terminal.dir("10").dir().toString(), is(equalTo("10")));
        assertThat(terminal.dir(new File("1010")).dir().toString(), is(equalTo("1010")));
        assertThat(terminal.dir(Paths.get("101010")).dir().toString(), is(equalTo("101010")));
    }

    @Test
    void addConsumerInfo_ShouldBeSuccessful() {
        assertThat(terminal.consumerInfoStream(System.out::println), is(notNullValue()));
        assertThat(terminal.consumerErrorStream(System.err::println), is(notNullValue()));
        terminal.execute("echo \"Howdy\"");
        terminal.breakOnError(false).execute("invalidCommand");
    }

    @Test
    void executeTwice_ShouldReturnMessagesFromBothCommands() {
        final String console = terminal.execute("echo \"Sub\"").execute("echo \"ject\"").consoleInfo();
        assertThat(console, containsString("Sub"));
        assertThat(console, containsString("ject"));
    }

    @Test
    void execute_ShouldContainSystemPropertiesAsWell() {
        System.setProperty("aa", "bb");
        final String console = terminal.timeoutMs(256).execute("echo $aa").consoleInfo();
        assertThat(console, containsString("bb"));
        assertThat(console, not(containsString("aa")));
    }

    @Test
    void copyOf_shouldCopyTerminal() {
        final Terminal input = new Terminal().waitFor(128);
        input.execute("echo \"Howdy\"");
        input.dir("inputDir");
        input.consumerInfoStream(System.out::println);
        input.consumerErrorStream(System.err::println);
        input.timeoutMs(256);
        final Terminal output = Terminal.copyOf(input);

        assertThat(input, is(not(equalTo(output))));
        assertThat(input.dir().toString(), is(containsString("inputDir")));
        assertThat(input.dir().toString(), is(equalTo(output.dir().toString())));
        assertThat(input.status(), is(equalTo(output.status())));
        assertThat(input.status(), is(equalTo(output.status())));
        assertThat(input.timeoutMs(), is(equalTo(output.timeoutMs())));
        assertThat(input.waitFor(), is(equalTo(output.waitFor())));
        assertThat(input.breakOnError(), is(equalTo(output.breakOnError())));
        assertThat((input.consoleInfo() + input.consoleError()).length(),
                is(not((output.consoleInfo() + output.consoleError()).length())));
    }
}
