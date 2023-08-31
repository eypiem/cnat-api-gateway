package dev.apma.cnat.apigateway.exception.trackerservice;


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
