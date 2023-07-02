package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.jwt.JwtRequest;
import dev.apma.cnat.apigateway.jwt.JwtResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping(path = "/auth")
public class JwtApiController {
    private final static Logger LOGGER = LoggerFactory.getLogger(JwtApiController.class);

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
        String uri = "http://localhost:8082/authenticate";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body = Map.of("username", email, "password", password);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email,
                        password)))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error in communicating with cnat-user-service.", e);
        }

        if ("true".equals(response.body())) {
            LOGGER.info("email found");
            return true;
        } else if ("false".equals(response.body())) {
            LOGGER.info("email not found");
            return false;
        }
        throw new RuntimeException("Bad response from cnat-user-service.");
    }
}