package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.TrackerData;
import dev.apma.cnat.apigateway.jwt.JwtClaims;
import dev.apma.cnat.apigateway.request.GetTrackerDataRequest;
import dev.apma.cnat.apigateway.request.TrackerDataRegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/tracker-data")
public class TrackerDataController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerDataController.class);

    @Value("${app.cnat.tracker-service}")
    private String trackerServiceUri;

    @Autowired
    private KafkaTemplate<String, TrackerDataRegisterRequest> kafkaTemplate;

    @Value("${app.kafka.topics.tracker-data-register}")
    private String trackerDataRegisterTopic;

    @PostMapping("/register")
    public void register(@RequestBody @NonNull TrackerDataRegisterRequest body) {
        LOGGER.info("/tracker-data/register: {}", body);
        kafkaTemplate.send(trackerDataRegisterTopic, body);
    }

    @PostMapping("/get")
    public TrackerData[] getTrackerData(Authentication auth, @RequestBody GetTrackerDataRequest body) {
        LOGGER.info("/tracker-data/get: {}", body);
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        if (!body.tracker().userId().equals(token.getTokenAttributes().get(JwtClaims.USER_EMAIL))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not correspond to tracker.");
        }
        String uri = trackerServiceUri + "/tracker-data/get";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GetTrackerDataRequest> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.postForObject(uri, request, TrackerData[].class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error in communicating with cnat-tracker-service.", e);
        }
    }
}
