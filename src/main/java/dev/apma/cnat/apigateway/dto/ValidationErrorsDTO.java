package dev.apma.cnat.apigateway.dto;


import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;


/**
 * This DTO class represents the validation error format returned from all CNAT services.
 *
 * @author Amir Parsa Mahdian
 */
public record ValidationErrorsDTO(List<ValidationError> validationErrors) {
    record ValidationError(String field, String error) {
        static ValidationError fromFieldError(FieldError error) {
            return new ValidationError(error.getField(),
                    error.getDefaultMessage() == null ? "" : error.getDefaultMessage());
        }
    }

    public static ValidationErrorsDTO fromFieldError(MethodArgumentNotValidException ex) {
        return new ValidationErrorsDTO(ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ValidationError::fromFieldError)
                .toList());
    }
}
