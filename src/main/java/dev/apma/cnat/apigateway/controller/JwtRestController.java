package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.jwt.JwtClaims;
import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/auth")
public class JwtRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(JwtRestController.class);

    @Value("${app.cnat.user-service}")
    private String userServiceUri;

    @Autowired
    private JwtHelper jwtHelper;

    @PostMapping("login")
    public ResponseEntity<UserAuthResponse> login(@RequestBody UserAuthRequest body) {
        Optional<Boolean> authResult = emailAndPasswordIsValid(body);
        if (authResult.isPresent()) {
            if (authResult.get()) {
                LOGGER.info("User authentication succeeded");
                Map<String, String> claims = new HashMap<>();
                claims.put(JwtClaims.USER_EMAIL, body.email());
                return new ResponseEntity<>(new UserAuthResponse(body.email(),
                        jwtHelper.createJwtForClaims(body.email(), claims)), HttpStatus.OK);
            } else {
                LOGGER.warn("User authentication failed");
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }
        } else {
            LOGGER.error("Error in communicating with cnat-user-service");
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Optional<Boolean> emailAndPasswordIsValid(UserAuthRequest req) {
        String uri = userServiceUri + "/authenticate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserAuthRequest> request = new HttpEntity<>(req, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            Boolean response = restTemplate.postForObject(uri, request, Boolean.class);
            return Optional.ofNullable(response);
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage());
        }
        return Optional.empty();
    }
}