package berlin.yuna.clu.model.exception;

public class FileNotReadableException extends RuntimeException {

    public FileNotReadableException(String message, Throwable cause) {
        super(message, cause);
    }
}
