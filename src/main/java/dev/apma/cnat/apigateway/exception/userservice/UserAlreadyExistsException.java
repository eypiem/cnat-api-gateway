package dev.apma.cnat.apigateway.exception.userservice;


/**
 * This {@code Exception} indicates a situation where an email has been reported as already existing by CNAT User
 * Service.
 *
 * @author Amir Parsa Mahdian
 */
public final class UserAlreadyExistsException extends UserServiceException {

    public UserAlreadyExistsException() {
        super("An account for that email already exists");
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
