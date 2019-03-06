package berlin.yuna.clu.logic;


import berlin.yuna.clu.logic.SystemUtil.OperatingSystem;
import berlin.yuna.clu.util.StreamGobbler;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;


public class Terminal {

    private Process process;
    private StringBuilder consoleInfo = new StringBuilder();
    private StringBuilder consoleError = new StringBuilder();

    private int status = 0;
    private long timeoutMs = -1;
    private boolean breakOnError = true;
    private File dir = new File(System.getProperty("user.dir"));
    private final List<Consumer<String>> consumerInfo = new ArrayList<>();
    private final List<Consumer<String>> consumerError = new ArrayList<>();

    private static final Logger LOG = getLogger(Terminal.class);
    private static final OperatingSystem OS_TYPE = SystemUtil.getOsType();

    public Terminal() {
        consumerInfo.add(LOG::info);
        consumerError.add(LOG::error);
        consumerInfo.add(consoleInfo::append);
        consumerError.add(consoleError::append);
    }

    /**
     * Clears the console output {@link Terminal#consoleInfo()} {@link Terminal#consoleError()}
     *
     * @return Terminal
     */
    public Terminal clearConsole() {
        consoleInfo = new StringBuilder();
        consoleError = new StringBuilder();
        return this;
    }

    /**
     * @param consumerInfo consumer for console info output
     * @return Terminal
     */
    @SafeVarargs
    public final Terminal consumerInfo(final Consumer<String>... consumerInfo) {
        this.consumerInfo.addAll(asList(consumerInfo));
        return this;
    }

    /**
     * @param consumerError consumer for console error output
     * @return Terminal
     */
    @SafeVarargs
    public final Terminal consumerError(final Consumer<String>... consumerError) {
        this.consumerError.addAll(asList(consumerError));
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
        return consoleInfo.toString();
    }

    /**
     * @return returns the console error output
     */
    public String consoleError() {
        return consoleError.toString();
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
        try {
            process = process(command);
            if (timeoutMs == -1L) {
                status = process.waitFor();
                LOG.debug("Terminal status [{}]", status);
            } else {
                waitFor(command);
            }
            return this;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a command with (sh or cmd.exe) ant he help of the {@link ProcessBuilder}
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

        Executors.newSingleThreadExecutor().submit(new StreamGobbler(process.getInputStream(), consumerInfo));
        Executors.newSingleThreadExecutor().submit(new StreamGobbler(process.getErrorStream(), consumerError));

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

        if (breakOnError && !consoleError.toString().isEmpty()) {
            throw new IllegalStateException("[" + dir.getName() + "] [" + command + "] " + consoleError.toString());
        } else if (!consoleError.toString().isEmpty()) {
            status = 2;
        }
    }

    private int countTerminalMessages() {
        return consoleInfo.length() + consoleError.length();
    }

}
