package dev.apma.cnat.apigateway.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping(path = "/tracker")
public class TrackerController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerController.class);

    @Value("${app.cnat.tracker-service}")
    private String trackerServiceUri;

    @PostMapping("/register")
    public void register(Authentication auth) {
        LOGGER.info("/tracker/register");
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        Map<String, Object> attributes = token.getTokenAttributes();

        String uri = trackerServiceUri + "/trackers/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<>("{\"userId\": \"%s\"}".formatted(attributes.get("email")), headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.postForObject(uri, request, String.class);
            LOGGER.info("response: {}", response);
        } catch (RestClientException e) {
            throw new RuntimeException("Error in communicating with cnat-tracker-service.", e);
        }
    }
}
