package dev.apma.cnat.apigateway.exception.trackerservice;


/**
 * This {@code Exception} indicates a situation where a tracker's owner differs from the user trying to perform a
 * request.
 *
 * @author Amir Parsa Mahdian
 */
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
