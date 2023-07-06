package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.Tracker;
import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.TrackerRegisterRequest;
import dev.apma.cnat.apigateway.response.GenericResponse;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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
    public TrackerRegisterResponse register(Authentication auth) {
        LOGGER.info("/tracker/register");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            try {
                String uri = trackerServiceUri + "/tracker/register";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<TrackerRegisterRequest> request =
                        new HttpEntity<>(new TrackerRegisterRequest(subject), headers);
                Tracker tracker = new RestTemplate().postForObject(uri, request, Tracker.class);
                if (tracker != null) {
                    Map<String, String> claims = new HashMap<>();
                    claims.put(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.TRACKER.toString());
                    return new TrackerRegisterResponse(tracker,
                            jwtHelper.createJwtForClaimsWithNoExpiry(tracker.id(), claims));
                }
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Bad response from cnat-tracker-service");
            } catch (HttpStatusCodeException e) {
                GenericResponse gr = e.getResponseBodyAs(GenericResponse.class);
                throw new ResponseStatusException(e.getStatusCode(), gr != null ? gr.message() : null);
            } catch (RestClientException e) {
                LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Error in communicating with cnat-tracker-service");
            }
        });
    }

    @GetMapping("/get")
    public Tracker[] getUserTrackers(Authentication auth) {
        LOGGER.info("/tracker/get");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            String uri = trackerServiceUri + "/tracker/get?userId=%s".formatted(subject);
            try {
                return new RestTemplate().getForObject(uri, Tracker[].class);
            } catch (RestClientException e) {
                LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Error in communicating with cnat-tracker-service");
            }
        });
    }
}
