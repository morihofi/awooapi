package net.fuxle.awooapi.exceptions;

public class AwooApiHandlerExecutionException extends AwooApiException {
    public AwooApiHandlerExecutionException() {
        super();
    }

    public AwooApiHandlerExecutionException(String message) {
        super(message);
    }

    public AwooApiHandlerExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AwooApiHandlerExecutionException(Throwable cause) {
        super(cause);
    }

    protected AwooApiHandlerExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
