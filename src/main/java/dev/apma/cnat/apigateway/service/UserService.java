package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.UserDTO;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.userservice.UserAlreadyExistsException;
import dev.apma.cnat.apigateway.exception.userservice.UserDoesNotExistException;
import dev.apma.cnat.apigateway.exception.userservice.UserServiceCommunicationException;
import dev.apma.cnat.apigateway.exception.userservice.UserServiceUnauthorizedException;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserDeleteRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.UserAuthResponse;

/**
 * This interface represents the CNAT User Service.
 *
 * @author Amir Parsa Mahdian
 */
public interface UserService {

    /**
     * Registers a new user.
     *
     * @param req a {@code UserRegisterRequest} containing the information of the user to be registered
     * @throws FieldValidationException          if CNAT User Service returns a validation error
     * @throws UserAlreadyExistsException        if a user is already registered with the provided email
     * @throws UserServiceCommunicationException if an unexpected error occurs during communication with CNAT User
     *                                           Service
     */
    void register(UserRegisterRequest req) throws FieldValidationException, UserAlreadyExistsException,
            UserServiceCommunicationException;
    
    /**
     * Returns the user having the provided email.
     *
     * @param email the user's email
     * @return the user having the provided email
     * @throws UserDoesNotExistException         if a user with the provided email does not exist
     * @throws UserServiceCommunicationException if an unexpected error occurs during communication with CNAT User
     *                                           Service
     */
    UserDTO getByEmail(String email) throws UserDoesNotExistException, UserServiceCommunicationException;

    /**
     * Authenticates a user.
     *
     * @param req a {@code UserDeleteRequest} containing the email and password of the user to be authenticated
     * @return A {@code UserAuthResponse} containing the user's email and access token
     * @throws FieldValidationException          if CNAT User Service returns a validation error
     * @throws UserServiceUnauthorizedException  if CNAT User Service returns an authorization error
     * @throws UserServiceCommunicationException if an unexpected error occurs during communication with CNAT User
     *                                           Service
     */
    UserAuthResponse auth(UserAuthRequest req) throws FieldValidationException, UserServiceUnauthorizedException,
            UserServiceCommunicationException;

    /**
     * Deletes a user.
     *
     * @param req a {@code UserDeleteRequest} containing the email and password of the user to be deleted
     * @throws FieldValidationException          if CNAT User Service returns a validation error
     * @throws UserServiceUnauthorizedException  if CNAT User Service returns an authorization error
     * @throws UserServiceCommunicationException if an unexpected error occurs during communication with CNAT User
     *                                           Service
     */
    void deleteUser(UserDeleteRequest req) throws FieldValidationException, UserServiceUnauthorizedException,
            UserServiceCommunicationException;
}
