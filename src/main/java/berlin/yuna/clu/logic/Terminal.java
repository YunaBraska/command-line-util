package berlin.yuna.clu.logic;


import berlin.yuna.clu.model.OsType;
import berlin.yuna.clu.model.exception.TerminalExecutionException;
import berlin.yuna.clu.util.StreamGobbler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;


@SuppressWarnings("unused")
public class Terminal {

    private Process process;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger status = new AtomicInteger(0);
    private final CommandOutput commandOutput = new CommandOutput();
    private final CommandOutput tmpOutput = new CommandOutput();

    private long timeoutMs = -1;
    private long waitForMs = 5;
    private boolean breakOnError = false;
    private File dir = new File(System.getProperty("user.dir"));

    /**
     * Clean copy of terminal with default consumer and clean console log
     *
     * @param terminal terminal to copy
     * @return new terminal copy
     */
    public static Terminal copyOf(final Terminal terminal) {
        final var result = new Terminal();
        result.breakOnError(terminal.breakOnError);
        result.timeoutMs(terminal.timeoutMs);
        result.status.set(terminal.status.get());
        result.waitForMs = terminal.waitForMs;
        result.dir(terminal.dir);
        return result;
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
     * @return set ms to wait after execution if the command is faster than logging its messages (default=5)
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
     * @return returns the console output as list
     */
    public List<String> consoleInfoList() {
        final var result = new ArrayList<>(commandOutput.consoleInfo);
        result.addAll(tmpOutput.consoleInfo);
        return result;
    }

    /**
     * @return returns the console error output
     */
    public String consoleError() {
        return commandOutput.consoleError() + tmpOutput.consoleError();
    }

    /**
     * @return returns the console error as list
     */
    public List<String> consoleErrorList() {
        final var result = new ArrayList<>(commandOutput.consoleError);
        result.addAll(tmpOutput.consoleError);
        return result;
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
    public synchronized Terminal execute(final String command, final long waitForMs) {
        try {
            running.set(true);
            process = process(command);
            process.onExit().thenApply(p -> {
                running.set(false);
                return p;
            });

            waitUntilDone(process, timeoutMs, waitForMs);
            status.set(clearTmpOutput(process));
            handleConsoleError(breakOnError, status.get(), command);
            return this;
        } catch (IOException | InterruptedException e) {
            throw new TerminalExecutionException("Failed to run dir command [" + command + "] in dir [" + dir.getName() + "]", e);
        } finally {
            running.set(false);
        }
    }

    private void handleConsoleError(final boolean breakOnError, final int status, final String command) {
        if (breakOnError && status != 0) {
            throw new IllegalStateException("Failed to run dir command [" + command + "] in dir [" + dir.getName() + "] output [" + tmpOutput.consoleError() + "]");
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
        final var builder = new ProcessBuilder();
        builder.directory(dir);
        System.getProperties().forEach((key, value) -> builder.environment().put(key.toString(), value.toString()));
        builder.command(addExecutor(SystemUtil.OS, command));
        final var result = builder.start();

        Executors.newSingleThreadExecutor().submit(new StreamGobbler(result.getInputStream(), singletonList(tmpOutput::consoleInfo)));
        Executors.newSingleThreadExecutor().submit(new StreamGobbler(result.getErrorStream(), singletonList(tmpOutput::consoleError)));

        return result;
    }

    /**
     * @return status code from last command {@link Process#waitFor()}
     */
    public int status() {
        return status.get();
    }

    String[] addExecutor(final OsType os, final String command) {
        if (os == OsType.OS_WINDOWS) {
            return new String[]{"cmd.exe", "/c", command};
        } else {
            return new String[]{"sh", "-c", command};
        }
    }

    public int messageCount() {
        return commandOutput.consoleInfo.size()
                + commandOutput.consoleError.size()
                + tmpOutput.consoleInfo.size()
                + tmpOutput.consoleError.size();
    }

    private synchronized void waitUntilDone(final Process process, final long timeoutMs, final long waitForMs) throws InterruptedException {
        process.waitFor(timeoutMs < 1 ? 10000 : timeoutMs, MILLISECONDS);
        final long waitMs = waitForMs < 1 ? 5 : waitForMs;
        while (running.get()) {
            this.wait(waitMs);
        }

        var count = messageCount();
        if ((waitForMs < 1 && count == 0) || waitForMs > 0) {
            do {
                count = messageCount();
                this.wait(waitMs);
            } while (count != messageCount());
        }
    }

    private int clearTmpOutput(final Process process) {
        int outputStatus;
        try {
            outputStatus = process.exitValue();
        } catch (IllegalThreadStateException e) {
            outputStatus = 0;
        }
        commandOutput.consoleInfo(tmpOutput.consoleInfo.toArray(new String[0]));
        if (outputStatus > 0) {
            commandOutput.consoleError(tmpOutput.consoleError.toArray(new String[0]));
        } else {
            commandOutput.consoleInfo(tmpOutput.consoleError.toArray(new String[0]));
        }
        tmpOutput.clear();
        return outputStatus;
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
            addToConsole(string, consoleInfo, consumerInfo);
        }

        void consoleError(final String... string) {
            addToConsole(string, consoleError, consumerError);
        }

        private void addToConsole(final String[] string, final List<String> consoleError, final List<Consumer<String>> consumerError) {
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
