package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.GenericResponse;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final String userServiceUri;

    private final JwtHelper jwtHelper;

    public UserService(@Value("${app.cnat.user-service}") String userServiceUri, JwtHelper jwtHelper) {
        this.userServiceUri = userServiceUri;
        this.jwtHelper = jwtHelper;
    }

    public UserAuthResponse auth(UserAuthRequest uar) {
        if (emailAndPasswordIsValid(uar)) {
            LOGGER.info("User authentication succeeded");
            Map<String, String> claims = new HashMap<>();
            claims.put(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.USER.toString());
            return new UserAuthResponse(uar.email(), jwtHelper.createJwtForClaims(uar.email(), claims));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication failed");
        }
    }

    public GenericResponse register(@RequestBody UserRegisterRequest urr) {
        String uri = userServiceUri + "/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserRegisterRequest> request = new HttpEntity<>(urr, headers);

        try {
            return new RestTemplate().postForObject(uri, request, GenericResponse.class);
        } catch (HttpStatusCodeException e) {
            GenericResponse gr = e.getResponseBodyAs(GenericResponse.class);
            throw new ResponseStatusException(e.getStatusCode(), gr != null ? gr.message() : null);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    private Boolean emailAndPasswordIsValid(UserAuthRequest uar) {
        String uri = userServiceUri + "/auth";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserAuthRequest> request = new HttpEntity<>(uar, headers);
        try {
            new RestTemplate().postForObject(uri, request, TrackerRegisterResponse.class);
            return true;
        } catch (HttpClientErrorException.Unauthorized e) {
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-user-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-user-service");
        }
    }
}
