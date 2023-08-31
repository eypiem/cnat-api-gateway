package dev.apma.cnat.apigateway.exception.trackerservice;


import dev.apma.cnat.apigateway.exception.CNATServiceException;

/**
 * This {@code Exception} indicates a situation where an error occurred while interacting with CNAT Tracker Service.
 *
 * @author Amir Parsa Mahdian
 */
public class TrackerServiceException extends CNATServiceException {

    public TrackerServiceException() {
        super();
    }

    public TrackerServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrackerServiceException(String message) {
        super(message);
    }

    public TrackerServiceException(Throwable cause) {
        super(cause);
    }
}
