package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.UserDTO;
import dev.apma.cnat.apigateway.dto.ValidationErrorsDTO;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.userservice.UserAlreadyExistsException;
import dev.apma.cnat.apigateway.exception.userservice.UserDoesNotExistException;
import dev.apma.cnat.apigateway.exception.userservice.UserServiceCommunicationException;
import dev.apma.cnat.apigateway.exception.userservice.UserServiceUnauthorizedException;
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

/**
 * An implementation of the {@code UserService} interface.
 *
 * @author Amir Parsa Mahdian
 * @see dev.apma.cnat.apigateway.service.UserService
 */
@Service
public class UserServiceImpl implements UserService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * The User Service Base REST API URI.
     */
    private final String userServiceUri;

    /**
     * The service used for issuing and validating JWTs.
     */
    private final JwtService jwtSvc;

    public UserServiceImpl(@Value("${app.cnat.user-service}") String userServiceUri, JwtService jwtSvc) {
        this.userServiceUri = userServiceUri;
        this.jwtSvc = jwtSvc;
    }

    @Override
    public void register(UserRegisterRequest req) throws UserServiceCommunicationException,
            UserAlreadyExistsException, FieldValidationException {
        var uri = userServiceUri;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(req, headers);

        try {
            new RestTemplate().exchange(uri, HttpMethod.POST, request, Object.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new FieldValidationException(e.getResponseBodyAs(ValidationErrorsDTO.class));
        } catch (HttpClientErrorException.Conflict e) {
            throw new UserAlreadyExistsException();
        } catch (RestClientException e) {
            throw new UserServiceCommunicationException();
        }
    }

    @Override
    public UserDTO getByEmail(String email) throws UserDoesNotExistException, UserServiceCommunicationException {
        var uri = userServiceUri + "?email=%s".formatted(email);

        try {
            return new RestTemplate().getForObject(uri, UserDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Access to non-existent user requested.");
            throw new UserDoesNotExistException();
        } catch (RestClientException e) {
            throw new UserServiceCommunicationException();
        }
    }

    @Override
    public UserAuthResponse auth(UserAuthRequest req) throws UserServiceCommunicationException,
            UserServiceUnauthorizedException, FieldValidationException {
        var uri = userServiceUri + "/auth";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(req, headers);

        try {
            new RestTemplate().exchange(uri, HttpMethod.POST, request, Object.class);
            var claims = Map.of(JwtServiceImpl.ROLE_ATTRIBUTE, JwtServiceImpl.Role.USER.toString());
            return new UserAuthResponse(req.email(), jwtSvc.createJwtForClaims(req.email(), claims));
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UserServiceUnauthorizedException();
        } catch (HttpClientErrorException.BadRequest e) {
            throw new FieldValidationException(e.getResponseBodyAs(ValidationErrorsDTO.class));
        } catch (RestClientException e) {
            throw new UserServiceCommunicationException();
        }
    }

    @Override
    public void deleteUser(UserDeleteRequest req) throws UserServiceCommunicationException,
            UserServiceUnauthorizedException, FieldValidationException {
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
            throw new UserServiceCommunicationException();
        }
    }
}
