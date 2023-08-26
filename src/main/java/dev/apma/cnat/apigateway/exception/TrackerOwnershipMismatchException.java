package dev.apma.cnat.apigateway.exception;


public class TrackerOwnershipMismatchException extends TrackerServiceException {

    public TrackerOwnershipMismatchException() {
        super();
    }

    public TrackerOwnershipMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrackerOwnershipMismatchException(String message) {
        super(message);
    }

    public TrackerOwnershipMismatchException(Throwable cause) {
        super(cause);
    }
}
