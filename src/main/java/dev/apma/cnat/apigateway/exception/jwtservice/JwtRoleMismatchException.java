package dev.apma.cnat.apigateway.exception.jwtservice;


public class JwtRoleMismatchException extends JwtServiceException {

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
