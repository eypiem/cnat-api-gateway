package dev.apma.cnat.apigateway.exception.userservice;


/**
 * This {@code Exception} indicates a situation where a request has been deemed unauthorized by CNAT User Service.
 *
 * @author Amir Parsa Mahdian
 */
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
