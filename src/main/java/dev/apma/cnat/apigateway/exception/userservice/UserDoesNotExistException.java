package dev.apma.cnat.apigateway.exception.userservice;


/**
 * This {@code Exception} indicates a situation where a requested user does not exist.
 *
 * @author Amir Parsa Mahdian
 */
public final class UserDoesNotExistException extends UserServiceException {

    public UserDoesNotExistException() {
        super("An account for that email already exists");
    }

    public UserDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDoesNotExistException(String message) {
        super(message);
    }

    public UserDoesNotExistException(Throwable cause) {
        super(cause);
    }
}
