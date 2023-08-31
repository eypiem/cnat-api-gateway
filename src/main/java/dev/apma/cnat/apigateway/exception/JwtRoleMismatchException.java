package dev.apma.cnat.apigateway.exception;


/**
 * This {@code Exception} indicates a situation where a JWT's role does not match the requirements.
 *
 * @author Amir Parsa Mahdian
 */
public class JwtRoleMismatchException extends Exception {

    public JwtRoleMismatchException() {
        super();
    }

    public JwtRoleMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtRoleMismatchException(String message) {
        super(message);
    }

    public JwtRoleMismatchException(Throwable cause) {
        super(cause);
    }
}
