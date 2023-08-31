package dev.apma.cnat.apigateway.exception.userservice;


public class UserServiceUnauthorizedException extends UserServiceException {

    public UserServiceUnauthorizedException() {
        super();
    }

    public UserServiceUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserServiceUnauthorizedException(String message) {
        super(message);
    }

    public UserServiceUnauthorizedException(Throwable cause) {
        super(cause);
    }
}
