package dev.apma.cnat.apigateway.exception;


import java.io.Serial;

public final class UserAlreadyExistsException extends UserServiceException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserAlreadyExistsException() {
        super();
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
