package dev.apma.cnat.apigateway.exception.userservice;


import dev.apma.cnat.apigateway.exception.CNATServiceException;

public class UserServiceException extends CNATServiceException {

    public UserServiceException() {
        super();
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(Throwable cause) {
        super(cause);
    }
}
