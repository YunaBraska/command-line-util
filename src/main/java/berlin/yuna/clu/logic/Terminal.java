package berlin.yuna.clu.logic;


import berlin.yuna.clu.logic.SystemUtil.OperatingSystem;
import berlin.yuna.clu.util.StreamGobbler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;


public class Terminal {

    private Process process;

    private final CommandOutput commandOutput = new CommandOutput();
    private final CommandOutput tmpOutput = new CommandOutput();

    private int status = 0;
    private long timeoutMs = -1;
    private long waitForMs = 256;
    private boolean breakOnError = false;
    private File dir = new File(System.getProperty("user.dir"));

    private static final OperatingSystem OS_TYPE = SystemUtil.getOsType();

    public Terminal() {
    }

    /**
     * Clean copy of terminal with default consumer and clean console log
     *
     * @param terminal terminal to copy
     * @return new terminal copy
     */
    public static Terminal copyOf(final Terminal terminal) {
        final Terminal term = new Terminal();
        term.breakOnError(terminal.breakOnError);
        term.timeoutMs(terminal.timeoutMs);
        term.status = terminal.status;
        term.waitForMs = terminal.waitForMs;
        term.dir(terminal.dir);
        return term;
    }

    /**
     * Clears the console output {@link Terminal#consoleInfo()} {@link Terminal#consoleError()}
     *
     * @return Terminal
     */
    public Terminal clearConsole() {
        commandOutput.clear();
        tmpOutput.clear();
        return this;
    }

    /**
     * @param consumerInfo consumer for console info stream
     * @return Terminal
     */
    @SafeVarargs
    public final Terminal consumerInfoStream(final Consumer<String>... consumerInfo) {
        this.tmpOutput.consumerInfo.addAll(asList(consumerInfo));
        return this;
    }

    /**
     * @param consumerError consumer for console error stream
     * @return Terminal
     */
    @SafeVarargs
    public final Terminal consumerErrorStream(final Consumer<String>... consumerError) {
        this.tmpOutput.consumerError.addAll(asList(consumerError));
        return this;
    }

    /**
     * @param consumerError consumer for console exit code info
     * @return Terminal
     */
    @SafeVarargs
    public final Terminal consumerInfo(final Consumer<String>... consumerError) {
        this.commandOutput.consumerInfo.addAll(asList(consumerError));
        return this;
    }

    /**
     * @param consumerError consumer for console exit code errors
     * @return Terminal
     */
    @SafeVarargs
    public final Terminal consumerError(final Consumer<String>... consumerError) {
        this.commandOutput.consumerError.addAll(asList(consumerError));
        return this;
    }

    /**
     * @return timeout in milliseconds
     * @see Terminal#timeoutMs(long)
     */
    public long timeoutMs() {
        return timeoutMs;
    }

    /**
     * Alternative to {@link Process#waitFor} as sometimes a process can be to fast or to slow for {@link Process#waitFor} or you need a timeout
     * Its combined with ({@link Terminal#breakOnError(boolean)})
     * Default : -1 (deactivated)
     * Also activates a heartbeat check (timeoutMs / 40) which will also timeout if there is no output is happening
     *
     * @param timeoutMs timeout in milliseconds
     * @return Terminal
     */
    public Terminal timeoutMs(final long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    /**
     * @return boolean of current state
     * @see Terminal#breakOnError(boolean)
     */
    public boolean breakOnError() {
        return breakOnError;
    }

    /**
     * Will throw {@link IllegalStateException} on error when true
     *
     * @param breakOnError set state
     * @return Terminal
     */
    public Terminal breakOnError(final boolean breakOnError) {
        this.breakOnError = breakOnError;
        return this;
    }

    /**
     * @return current working directory
     * @see Terminal#dir(File)
     */
    public File dir() {
        return dir;
    }

    /**
     * @return wait time after command exited
     * @see Terminal#execute(String, long)
     */
    public long waitFor() {
        return waitForMs;
    }

    /**
     * @return set ms to wait after execution if the command is faster than logging its messages (default=256)
     * @see Terminal#execute(String, long)
     */
    public Terminal waitFor(final long waitForMs) {
        this.waitForMs = waitForMs;
        return this;
    }

    /**
     * @param dir sets the working directory
     * @return Terminal
     */
    public Terminal dir(final String dir) {
        this.dir = new File(dir);
        return this;
    }

    /**
     * @param dir sets the working directory
     * @return Terminal
     */
    public Terminal dir(final File dir) {
        this.dir = dir;
        return this;
    }

    /**
     * @param dir sets the working directory
     * @return Terminal
     */
    public Terminal dir(final Path dir) {
        this.dir = dir.toFile();
        return this;
    }

    /**
     * @return the currently used {@link Process} - return null when no command was executed
     */
    public Process process() {
        return process;
    }

    /**
     * @return returns the console output
     */
    public String consoleInfo() {
        return commandOutput.consoleInfo() + tmpOutput.consoleInfo();
    }

    /**
     * @return returns the console error output
     */
    public String consoleError() {
        return commandOutput.consoleError() + tmpOutput.consoleError();
    }

    /**
     * Executes a command with (sh or cmd.exe) ant he help of the {@link ProcessBuilder}
     * Default working directory: user.dir
     * {@link Terminal#timeoutMs(long)} if timeout is needed
     *
     * @param command command to execute
     * @return a new {@link Process} object for managing the sub process
     */
    public Terminal execute(final String command) {
        return execute(command, waitForMs);
    }

    /**
     * Executes a command with (sh or cmd.exe) ant he help of the {@link ProcessBuilder}
     * Default working directory: user.dir
     * {@link Terminal#timeoutMs(long)} if timeout is needed
     *
     * @param command   command to execute
     * @param waitForMs overwrites default {@link Terminal#waitFor(long)} for this call
     * @return a new {@link Process} object for managing the sub process
     */
    public Terminal execute(final String command, final long waitForMs) {
        try {
            process = process(command);
            if (timeoutMs == -1L) {
                process.waitFor();
            } else {
                waitFor(command);
            }
            process.onExit().thenApply(p -> clearTmpOutput());

            waitForConsoleOutput(waitForMs <= 0 ? 256 : waitForMs);
            final String error = tmpOutput.consoleError();
            status = clearTmpOutput();
            if (breakOnError && status != 0) {
                throw new IllegalStateException("[" + dir.getName() + "] [" + command + "] " + error);
            }
            return this;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a command with (sh or cmd.exe) with the help of the {@link ProcessBuilder}
     *
     * @param command command to execute
     * @return a new {@link Process} object for managing the sub process
     * @throws IOException if an I/O error occurs
     */
    public Process process(final String command) throws IOException {
        final ProcessBuilder builder = new ProcessBuilder();
        builder.directory(dir);
        System.getProperties().forEach((key, value) -> builder.environment().put(key.toString(), value.toString()));
        builder.command(addExecutor(OS_TYPE, command));
        final Process process = builder.start();

        Executors.newSingleThreadExecutor().submit(new StreamGobbler(process.getInputStream(), singletonList(tmpOutput::consoleInfo)));
        Executors.newSingleThreadExecutor().submit(new StreamGobbler(process.getErrorStream(), singletonList(tmpOutput::consoleError)));

        return process;
    }

    /**
     * @return status code from last command {@link Process#waitFor()}
     */
    public int status() {
        return status;
    }

    String[] addExecutor(final OperatingSystem os, final String command) {
        if (os == OperatingSystem.WINDOWS) {
            return new String[]{"cmd.exe", "/c", command};
        } else {
            return new String[]{"sh", "-c", command};
        }
    }

    private void waitFor(final String command) throws InterruptedException {
        status = 0;
        int count;
        final long startTime = System.currentTimeMillis();
        do {
            count = countTerminalMessages();
            Thread.sleep(timeoutMs / 40);
        }
        while ((count == 0 || count != (countTerminalMessages())) && (System.currentTimeMillis() - startTime) < timeoutMs);

        if ((System.currentTimeMillis() - startTime) > timeoutMs) {
            throw new RuntimeException(new TimeoutException("Execution got timed out [" + command + "]"));
        }
    }

    public int countTerminalMessages() {
        return commandOutput.consoleInfo.size()
                + commandOutput.consoleError.size()
                + tmpOutput.consoleInfo.size()
                + tmpOutput.consoleError.size();
    }

    private void waitForConsoleOutput(final long waitForMs) throws InterruptedException {
        int count;
        do {
            count = countTerminalMessages();
            Thread.sleep(waitForMs);
        } while (count != countTerminalMessages());
    }

    private int clearTmpOutput() {
        int status;
        try {
            status = process.exitValue();
        } catch (IllegalThreadStateException e) {
            status = 0;
        }
        commandOutput.consoleInfo(tmpOutput.consoleInfo.toArray(String[]::new));
        if (status > 0) {
            commandOutput.consoleError(tmpOutput.consoleError.toArray(String[]::new));
        } else {
            commandOutput.consoleInfo(tmpOutput.consoleError.toArray(String[]::new));
        }
        tmpOutput.clear();
        return status;
    }

    public static class CommandOutput {
        //TODO: List of TimeNs/CharSequence to merge easier non errors in error stream with info at clearTmpOutput
        final List<String> consoleInfo = new ArrayList<>();
        final List<String> consoleError = new ArrayList<>();
        final List<Consumer<String>> consumerInfo = new ArrayList<>();
        final List<Consumer<String>> consumerError = new ArrayList<>();

        String consoleInfo() {
            return String.join("", consoleInfo);
        }

        String consoleError() {
            return String.join("", consoleError);
        }

        void consoleInfo(final String... string) {
            stream(string).forEach(s -> {
                consoleInfo.add(s);
                consumerInfo.forEach(c -> c.accept(s));
            });
        }

        void consoleError(final String... string) {
            stream(string).forEach(s -> {
                consoleError.add(s);
                consumerError.forEach(c -> c.accept(s));
            });
        }

        void clear() {
            consoleInfo.clear();
            consoleError.clear();
        }
    }
}
