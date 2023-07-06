package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.TrackerData;
import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.TrackerDataGetRequest;
import dev.apma.cnat.apigateway.request.TrackerDataRegisterRequest;
import dev.apma.cnat.apigateway.response.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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
    public GenericResponse register(Authentication auth, @RequestBody TrackerDataRegisterRequest body) {
        LOGGER.info("/tracker-data/register: {}", body);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.TRACKER, (subject) -> {
            if (!body.trackerId().equals(subject)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token does not correspond to tracker");
            }
            kafkaTemplate.send(trackerDataRegisterTopic, body);
            return new GenericResponse("OK");
        });
    }

    @PostMapping("/get")
    public TrackerData[] getTrackerData(Authentication auth, @RequestBody TrackerDataGetRequest body) {
        LOGGER.info("/tracker-data/get: {}", body);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            if (!body.tracker().userId().equals(subject)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not correspond to tracker");
            }

            String uri = trackerServiceUri + "/tracker-data/get";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TrackerDataGetRequest> request = new HttpEntity<>(body, headers);
            try {
                return new RestTemplate().postForObject(uri, request, TrackerData[].class);
            } catch (RestClientException e) {
                LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Error in communicating with cnat-tracker-service");
            }
        });
    }
}
