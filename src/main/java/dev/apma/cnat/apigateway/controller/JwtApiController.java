package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.jwt.JwtRequest;
import dev.apma.cnat.apigateway.jwt.JwtResponse;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping(path = "/auth")
public class JwtApiController {
    private final static Logger LOGGER = LoggerFactory.getLogger(JwtApiController.class);

    @Value("${app.cnat.user-service}")
    private String userServiceUri;

    @Autowired
    private JwtHelper jwtHelper;

    @PostMapping(path = "login", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public JwtResponse login(@RequestBody JwtRequest body) {

        if (emailAndPasswordIsValid(body.email(), body.password())) {
            Map<String, String> claims = new HashMap<>();
            claims.put("email", body.email());
            return new JwtResponse(body.email(), jwtHelper.createJwtForClaims(body.email(), claims));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
    }

    private boolean emailAndPasswordIsValid(String email, String password) {
        String uri = userServiceUri + "/authenticate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<>("{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password), headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.postForObject(uri, request, String.class);
            if ("true".equals(response)) {
                LOGGER.info("user authenticated");
                return true;
            } else if ("false".equals(response)) {
                LOGGER.info("user not authenticated");
                return false;
            }
            throw new RuntimeException("Bad response from cnat-user-service.");
        } catch (RestClientException e) {
            throw new RuntimeException("Error in communicating with cnat-user-service.", e);
        }
    }
}