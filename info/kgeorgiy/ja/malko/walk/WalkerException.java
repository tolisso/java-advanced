package info.kgeorgiy.ja.malko.walk;

public class WalkerException extends RuntimeException {
    public WalkerException(final String message) {
        super(message);
    }

    public WalkerException(final String message, final Throwable cause) {
        super(message + ": " + cause.getMessage(), cause);
    }
}
