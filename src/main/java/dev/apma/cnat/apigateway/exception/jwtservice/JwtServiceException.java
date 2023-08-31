package dev.apma.cnat.apigateway.exception.jwtservice;


import dev.apma.cnat.apigateway.exception.CNATServiceException;

public class JwtServiceException extends CNATServiceException {

    public JwtServiceException() {
        super();
    }

    public JwtServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtServiceException(String message) {
        super(message);
    }

    public JwtServiceException(Throwable cause) {
        super(cause);
    }
}
