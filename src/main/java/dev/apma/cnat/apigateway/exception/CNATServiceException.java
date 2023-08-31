package dev.apma.cnat.apigateway.exception;


/**
 * This {@code Exception} indicates a situation where an error occurred while interacting with an external CNAT service.
 *
 * @author Amir Parsa Mahdian
 */
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
