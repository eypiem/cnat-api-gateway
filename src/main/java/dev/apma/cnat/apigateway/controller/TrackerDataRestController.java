package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.TrackerData;
import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.TrackerDataGetRequest;
import dev.apma.cnat.apigateway.request.TrackerDataRegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(path = "/tracker-data")
public class TrackerDataRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerDataRestController.class);

    @Value("${app.cnat.tracker-service}")
    private String trackerServiceUri;

    @Autowired
    private KafkaTemplate<String, TrackerDataRegisterRequest> kafkaTemplate;

    @Value("${app.kafka.topics.tracker-data-register}")
    private String trackerDataRegisterTopic;

    @PostMapping("/register")
    public ResponseEntity<String> register(Authentication auth, @RequestBody TrackerDataRegisterRequest body) {
        LOGGER.info("/tracker-data/register: {}", body);
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;

        return JwtHelper.onRoleMatch(token, JwtHelper.Role.TRACKER, () -> {
            if (!body.trackerId().equals(JwtHelper.getSubject(token))) {
                LOGGER.warn("JWT Token does not correspond to tracker");
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            kafkaTemplate.send(trackerDataRegisterTopic, body);
            return new ResponseEntity<>("{\"status\": \"Ok\"}", HttpStatus.OK);
        }, () -> {
            LOGGER.warn("Request from non-matching role");
            return new ResponseEntity<>("{\"error\": \"Role does not match\"}", HttpStatus.UNAUTHORIZED);
        });
    }

    @PostMapping("/get")
    public ResponseEntity<TrackerData[]> getTrackerData(Authentication auth, @RequestBody TrackerDataGetRequest body) {
        LOGGER.info("/tracker-data/get: {}", body);
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;

        return JwtHelper.onRoleMatch(token, JwtHelper.Role.USER, () -> {
            if (!body.tracker().userId().equals(JwtHelper.getSubject(token))) {
                LOGGER.warn("User does not correspond to tracker");
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            String uri = trackerServiceUri + "/tracker-data/get";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TrackerDataGetRequest> request = new HttpEntity<>(body, headers);
            try {
                return new ResponseEntity<>(new RestTemplate().postForObject(uri, request, TrackerData[].class),
                        HttpStatus.OK);
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
