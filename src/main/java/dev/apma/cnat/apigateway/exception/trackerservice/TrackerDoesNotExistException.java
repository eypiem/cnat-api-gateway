package dev.apma.cnat.apigateway.exception.trackerservice;


/**
 * This {@code Exception} indicates a situation where a non-existent tracker has been requested from CNAT Tracker
 * Service.
 *
 * @author Amir Parsa Mahdian
 */
public class TrackerDoesNotExistException extends TrackerServiceException {

    public TrackerDoesNotExistException() {
        super();
    }

    public TrackerDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrackerDoesNotExistException(String message) {
        super(message);
    }

    public TrackerDoesNotExistException(Throwable cause) {
        super(cause);
    }
}
