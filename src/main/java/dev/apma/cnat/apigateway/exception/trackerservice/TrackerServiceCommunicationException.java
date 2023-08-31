package dev.apma.cnat.apigateway.exception.trackerservice;


/**
 * This {@code Exception} indicates a situation where a connection error occurred while trying to communicate with
 * CNAT Tracker Service.
 *
 * @author Amir Parsa Mahdian
 */
public class TrackerServiceCommunicationException extends TrackerServiceException {

    public TrackerServiceCommunicationException() {
        super("Error in communicating with CNAT Tracker Service");
    }

    public TrackerServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrackerServiceCommunicationException(String message) {
        super(message);
    }

    public TrackerServiceCommunicationException(Throwable cause) {
        super(cause);
    }
}
