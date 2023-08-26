package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.ValidationErrorsDTO;
import dev.apma.cnat.apigateway.exception.*;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@Nullable MethodArgumentNotValidException ex,
                                                                  @Nullable HttpHeaders headers,
                                                                  @Nullable HttpStatusCode status,
                                                                  @Nullable WebRequest request) {
        return new ResponseEntity<>(ex == null ? null : ValidationErrorsDTO.fromFieldError(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {FieldValidationException.class})
    protected ResponseEntity<ValidationErrorsDTO> handleFieldValidationException(FieldValidationException ex) {
        return new ResponseEntity<>(ex.getErrors(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UserServiceUnauthorizedException.class})
    protected ResponseEntity<Object> handleUserServiceUnauthorizedException(UserServiceUnauthorizedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {UserAlreadyExistsException.class})
    protected ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {TrackerOwnershipMismatchException.class})
    protected ResponseEntity<Object> handleTrackerOwnershipMismatchException(TrackerOwnershipMismatchException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {CNATServiceException.class})
    protected ResponseEntity<Object> handleCNATServiceException(CNATServiceException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {JwtRoleMismatchException.class})
    protected ResponseEntity<Object> handleJwtRoleMismatchException(JwtRoleMismatchException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
