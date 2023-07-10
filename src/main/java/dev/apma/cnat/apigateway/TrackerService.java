package dev.apma.cnat.apigateway;


import dev.apma.cnat.apigateway.dto.Tracker;
import dev.apma.cnat.apigateway.dto.TrackerData;
import dev.apma.cnat.apigateway.jwt.JwtHelper;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TrackerService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerService.class);

    @Value("${app.cnat.tracker-service}")
    private String trackerServiceUri;

    @Autowired
    private JwtHelper jwtHelper;

    public Tracker getTrackerById(String trackerId) {
        String uri = trackerServiceUri + "/tracker/get/%s".formatted(trackerId);
        try {
            return new RestTemplate().getForObject(uri, Tracker.class);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public TrackerData[] getTrackerData(String trackerId, Optional<Instant> from, Optional<Instant> to) {
        try {
            String uri = trackerServiceUri + "/tracker-data/get/%s?".formatted(trackerId);

            if (from.isPresent()) {
                uri += "from=%s&".formatted(from.get());
            }
            if (to.isPresent()) {
                uri += "from=%s&".formatted(to.get());
            }

            return new RestTemplate().getForObject(uri, TrackerData[].class);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public void deleteTrackerById(String trackerId) {
        String uri = trackerServiceUri + "/tracker/delete/%s".formatted(trackerId);
        try {
            new RestTemplate().delete(uri);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public TrackerRegisterResponse registerTracker(Tracker body) {
        try {
            String uri = trackerServiceUri + "/tracker/register";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Tracker> request = new HttpEntity<>(body, headers);
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
    }

    public Tracker[] getUserTrackers(String userId) {
        String uri = trackerServiceUri + "/tracker/get?userId=%s".formatted(userId);
        try {
            return new RestTemplate().getForObject(uri, Tracker[].class);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }
}
