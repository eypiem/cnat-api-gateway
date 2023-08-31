package dev.apma.cnat.apigateway.exception.userservice;


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
