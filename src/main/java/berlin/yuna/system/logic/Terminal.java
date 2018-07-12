package berlin.yuna.system.logic;


import berlin.yuna.system.logic.SystemUtil.OperatingSystem;
import berlin.yuna.system.util.StreamGobbler;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;


public class Terminal {

    private Process process;
    private StringBuilder consoleInfo = new StringBuilder();
    private StringBuilder consoleError = new StringBuilder();

    private long timeoutMs = 10000;
    private boolean breakOnError = true;
    private File dir = new File(System.getProperty("user.dir"));
    private List<Consumer<String>> consumerInfo = new ArrayList<>();
    private List<Consumer<String>> consumerError = new ArrayList<>();

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
     */
    public void clearConsole() {
        consoleInfo = new StringBuilder();
        consoleError = new StringBuilder();
    }

    /**
     * @param consumerInfo consumer for console info output
     * @return Terminal
     */
    public Terminal consumerInfo(final Consumer<String>... consumerInfo) {
        this.consumerInfo.addAll(Arrays.asList(consumerInfo));
        return this;
    }

    /**
     * @param consumerError consumer for console error output
     * @return Terminal
     */
    public Terminal consumerError(final Consumer<String>... consumerError) {
        this.consumerError.addAll(Arrays.asList(consumerError));
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
     * soft timeout to wait for the log being finished.
     * Also used for heartbeat check (timeoutMs / 40) which will also timeout if there is no logging
     *
     * @param timeoutMs timeout in milliseconds
     * @return Terminal
     */
    public Terminal timeoutMs(long timeoutMs) {
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
    public Terminal breakOnError(boolean breakOnError) {
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
    public Terminal dir(String dir) {
        this.dir = new File(dir);
        return this;
    }

    /**
     * @param dir sets the working directory
     * @return Terminal
     */
    public Terminal dir(File dir) {
        this.dir = dir;
        return this;
    }

    /**
     * @param dir sets the working directory
     * @return Terminal
     */
    public Terminal dir(Path dir) {
        this.dir = dir.toFile();
        return this;
    }

    /**
     * @return the currently used {@Link Process} - return null when no command was executed
     */
    public Process process() {
        return process;
    }

    /**
     * @return returns the console output
     */
    public StringBuilder consoleInfo() {
        return consoleInfo;
    }

    /**
     * @return returns the console error output
     */
    public StringBuilder consoleError() {
        return consoleError;
    }

    /**
     * Executes a command with (sh or cmd.exe) ant he help of the {@link ProcessBuilder}
     * Default working directory: user.dir
     * Default breakOnError: true
     * Default soft timeout: will stop waiting for command when 250ms when no logging happens or the command runs 10s
     *
     * @param command command to execute
     * @return a new {@link Process} object for managing the subprocess
     */
    public Process execute(final String command) {
        try {
            process = process(command);
            int count;
            long startTime = System.currentTimeMillis();
            do {
                count = countTerminalMessages();
                Thread.sleep(timeoutMs / 40);
            }
            while ((count == 0 || count != (countTerminalMessages())) && (System.currentTimeMillis() - startTime) < timeoutMs);

            if (breakOnError && !consoleError.toString().isEmpty()) {
                throw new IllegalStateException("[" + dir.getName() + "] [" + command + "] " + consoleError.toString());
            }
            return process;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a command with (sh or cmd.exe) ant he help of the {@link ProcessBuilder}
     *
     * @param command command to execute
     * @return a new {@link Process} object for managing the subprocess
     * @throws IOException if an I/O error occurs
     */
    public Process process(final String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(dir);
        builder.command(addExecutor(OS_TYPE, command));
        Process process = builder.start();

        Executors.newSingleThreadExecutor().submit(new StreamGobbler(process.getInputStream(), consumerInfo));
        Executors.newSingleThreadExecutor().submit(new StreamGobbler(process.getErrorStream(), consumerError));

        return process;
    }

    String[] addExecutor(final OperatingSystem os, final String command) {
        if (os == OperatingSystem.WINDOWS) {
            return new String[]{"cmd.exe", "/c", command};
        } else {
            return new String[]{"sh", "-c", command};
        }
    }

    private int countTerminalMessages() {
        return consoleInfo.length() + consoleError.length();
    }

}
