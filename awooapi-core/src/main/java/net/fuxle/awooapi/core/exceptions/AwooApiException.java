package net.fuxle.awooapi.core.exceptions;

public class AwooApiException extends Exception {
    public AwooApiException() {
        super();
    }

    public AwooApiException(String message) {
        super(message);
    }

    public AwooApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public AwooApiException(Throwable cause) {
        super(cause);
    }

    protected AwooApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
