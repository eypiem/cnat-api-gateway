package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class JwtRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(JwtRestController.class);

    @Value("${app.cnat.user-service}")
    private String userServiceUri;

    @Autowired
    private JwtHelper jwtHelper;

    @PostMapping("login")
    public UserAuthResponse login(@RequestBody UserAuthRequest body) {
        if (emailAndPasswordIsValid(body)) {
            LOGGER.info("User authentication succeeded");
            Map<String, String> claims = new HashMap<>();
            claims.put(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.USER.toString());
            return new UserAuthResponse(body.email(), jwtHelper.createJwtForClaims(body.email(), claims));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication failed");
        }
    }

    private Boolean emailAndPasswordIsValid(UserAuthRequest req) {
        String uri = userServiceUri + "/authenticate";
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
