package dev.apma.cnat.apigateway.exception.userservice;


public class UserServiceCommunicationException extends UserServiceException {

    public UserServiceCommunicationException() {
        super("Error in communicating with CNAT User Service");
    }

    public UserServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserServiceCommunicationException(String message) {
        super(message);
    }

    public UserServiceCommunicationException(Throwable cause) {
        super(cause);
    }
}
