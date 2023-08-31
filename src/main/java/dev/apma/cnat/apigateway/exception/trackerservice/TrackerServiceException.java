package dev.apma.cnat.apigateway.exception.trackerservice;


import dev.apma.cnat.apigateway.exception.CNATServiceException;

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
