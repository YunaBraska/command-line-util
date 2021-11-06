package berlin.yuna.clu.model.exception;

public class TerminalExecutionException extends RuntimeException {

    public TerminalExecutionException(final String message) {
        super(message);
    }

    public TerminalExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
