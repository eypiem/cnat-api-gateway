package dev.apma.cnat.apigateway.exception;


import dev.apma.cnat.apigateway.dto.ValidationErrorsDTO;

public class FieldValidationException extends CNATServiceException {
    private final ValidationErrorsDTO errors;

    public FieldValidationException(ValidationErrorsDTO errors) {
        this.errors = errors;
    }

    public FieldValidationException(String message, ValidationErrorsDTO errors) {
        super(message);
        this.errors = errors;
    }

    public FieldValidationException(String message, Throwable cause, ValidationErrorsDTO errors) {
        super(message, cause);
        this.errors = errors;
    }

    public FieldValidationException(Throwable cause, ValidationErrorsDTO errors) {
        super(cause);
        this.errors = errors;
    }

    public ValidationErrorsDTO getErrors() {
        return errors;
    }
}
