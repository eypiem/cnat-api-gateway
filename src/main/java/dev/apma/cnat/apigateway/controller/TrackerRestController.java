package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.Tracker;
import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.TrackerRegisterRequest;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tracker")
public class TrackerRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerRestController.class);

    @Value("${app.cnat.tracker-service}")
    private String trackerServiceUri;

    @Autowired
    private JwtHelper jwtHelper;

    @PostMapping("/register")
    public ResponseEntity<TrackerRegisterResponse> register(Authentication auth) {
        LOGGER.info("/tracker/register");
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        String subject = JwtHelper.getSubject(token);

        String uri = trackerServiceUri + "/tracker/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TrackerRegisterRequest> request = new HttpEntity<>(new TrackerRegisterRequest(subject), headers);

        return JwtHelper.onRoleMatch(token, JwtHelper.Role.USER, () -> {
            try {
                Tracker tracker = new RestTemplate().postForObject(uri, request, Tracker.class);
                if (tracker != null) {
                    Map<String, String> claims = new HashMap<>();
                    claims.put(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.TRACKER.toString());
                    return new ResponseEntity<>(new TrackerRegisterResponse(tracker,
                            jwtHelper.createJwtForClaimsWithNoExpiry(tracker.id(), claims)), HttpStatus.OK);
                }
                LOGGER.error("Bad response from cnat-tracker-service");
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (RestClientException e) {
                LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }, () -> {
            LOGGER.warn("Request from non-matching role");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        });
    }

    @GetMapping("/get")
    public ResponseEntity<Tracker[]> getUserTrackers(Authentication auth) {
        LOGGER.info("/tracker/get");
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        String subject = JwtHelper.getSubject(token);

        return JwtHelper.onRoleMatch(token, JwtHelper.Role.USER, () -> {
            String uri = trackerServiceUri + "/tracker/get?userId=%s".formatted(subject);
            try {
                return new ResponseEntity<>(new RestTemplate().getForObject(uri, Tracker[].class), HttpStatus.OK);
            } catch (RestClientException e) {
                LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }, () -> {
            LOGGER.warn("Request from non-matching role");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        });
    }
}
