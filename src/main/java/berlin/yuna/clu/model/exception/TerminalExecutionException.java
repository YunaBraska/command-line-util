package berlin.yuna.clu.model.exception;

public class TerminalExecutionException extends RuntimeException {

    public TerminalExecutionException(final String message) {
        super(message);
    }

    public TerminalExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
