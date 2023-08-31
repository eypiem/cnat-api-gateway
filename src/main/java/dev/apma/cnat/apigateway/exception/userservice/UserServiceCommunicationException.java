package dev.apma.cnat.apigateway.exception.userservice;


/**
 * This {@code Exception} indicates a situation where a connection error occurred while trying to communicate with
 * CNAT User Service.
 *
 * @author Amir Parsa Mahdian
 */
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
