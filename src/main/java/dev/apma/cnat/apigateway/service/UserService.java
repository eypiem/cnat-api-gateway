package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.UserServiceException;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserDeleteRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.UserAuthResponse;

public interface UserService {

    UserAuthResponse auth(UserAuthRequest req) throws UserServiceException, FieldValidationException;

    void register(UserRegisterRequest req) throws UserServiceException, FieldValidationException;

    void deleteUser(UserDeleteRequest req) throws UserServiceException, FieldValidationException;
}
