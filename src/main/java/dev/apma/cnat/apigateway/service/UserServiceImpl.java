package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.ValidationErrorsDTO;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.UserAlreadyExistsException;
import dev.apma.cnat.apigateway.exception.UserServiceException;
import dev.apma.cnat.apigateway.exception.UserServiceUnauthorizedException;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserDeleteRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final String userServiceUri;

    private final JwtHelperImpl jwtHelper;

    public UserServiceImpl(@Value("${app.cnat.user-service}") String userServiceUri, JwtHelperImpl jwtHelper) {
        this.userServiceUri = userServiceUri;
        this.jwtHelper = jwtHelper;
    }

    public UserAuthResponse auth(UserAuthRequest req) throws UserServiceException, FieldValidationException {
        var uri = userServiceUri + "/auth";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(req, headers);

        try {
            new RestTemplate().exchange(uri, HttpMethod.POST, request, Object.class);
            var claims = Map.of(JwtHelperImpl.ROLE_ATTRIBUTE, JwtHelperImpl.Role.USER.toString());
            return new UserAuthResponse(req.email(), jwtHelper.createJwtForClaims(req.email(), claims));
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UserServiceUnauthorizedException();
        } catch (HttpClientErrorException.BadRequest e) {
            throw new FieldValidationException(e.getResponseBodyAs(ValidationErrorsDTO.class));
        } catch (RestClientException e) {
            throw new UserServiceException("Error in communicating with cnat-user-service");
        }
    }

    public void register(UserRegisterRequest req) throws UserServiceException, FieldValidationException {
        var uri = userServiceUri;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(req, headers);

        try {
            new RestTemplate().exchange(uri, HttpMethod.POST, request, Object.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new FieldValidationException(e.getResponseBodyAs(ValidationErrorsDTO.class));
        } catch (HttpClientErrorException.Conflict e) {
            throw new UserAlreadyExistsException("An account for that email already exists");
        } catch (RestClientException e) {
            throw new UserServiceException("Error in communicating with cnat-user-service");
        }
    }

    @Override
    public void deleteUser(UserDeleteRequest req) throws UserServiceException, FieldValidationException {
        String uri = userServiceUri;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(req, headers);

        try {
            new RestTemplate().exchange(uri, HttpMethod.DELETE, request, Object.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new FieldValidationException(e.getResponseBodyAs(ValidationErrorsDTO.class));
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UserServiceUnauthorizedException();
        } catch (RestClientException e) {
            throw new UserServiceException("Error in communicating with cnat-user-service");
        }
    }
}
