package berlin.yuna.clu.model.exception;

public class FileNotReadableException extends RuntimeException {

    public FileNotReadableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
