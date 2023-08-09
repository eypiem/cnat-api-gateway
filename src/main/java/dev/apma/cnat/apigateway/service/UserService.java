package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Value("${app.cnat.user-service}")
    private String userServiceUri;

    public Boolean emailAndPasswordIsValid(UserAuthRequest req) {
        String uri = userServiceUri + "/auth";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserAuthRequest> request = new HttpEntity<>(req, headers);
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
