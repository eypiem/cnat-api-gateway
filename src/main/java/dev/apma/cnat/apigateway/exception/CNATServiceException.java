package dev.apma.cnat.apigateway.exception;


public class CNATServiceException extends Exception {

    public CNATServiceException() {
        super();
    }

    public CNATServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CNATServiceException(String message) {
        super(message);
    }

    public CNATServiceException(Throwable cause) {
        super(cause);
    }
}
