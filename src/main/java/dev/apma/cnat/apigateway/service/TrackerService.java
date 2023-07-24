package dev.apma.cnat.apigateway.service;


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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class TrackerService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerService.class);

    @Value("${app.cnat.tracker-service}")
    private String trackerServiceUri;

    @Autowired
    private JwtHelper jwtHelper;

    public <T> T onTrackerMatchUserOrElseThrow(String trackerId, String userId, Supplier<T> onMatch) {
        var tracker = getTrackerById(trackerId);
        if (!userId.equals(tracker.userId())) {
            LOGGER.warn("Non-matching user and tracker request detected.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return onMatch.get();
    }

    public void onTrackerMatchUserOrElseThrow(String trackerId, String userId, Runnable onMatch) {
        onTrackerMatchUserOrElseThrow(trackerId, userId, () -> {
            onMatch.run();
            return null;
        });
    }

    public Tracker getTrackerById(String trackerId) {
        var uri = trackerServiceUri + "/tracker/get/%s".formatted(trackerId);
        try {
            return new RestTemplate().getForObject(uri, Tracker.class);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Access to non-existent tracker requested.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public TrackerData[] getTrackerData(String trackerId, Optional<Instant> from, Optional<Instant> to) {
        try {
            var uri = trackerServiceUri + "/tracker-data/get/%s?".formatted(trackerId);

            if (from.isPresent()) {
                uri += "from=%s&".formatted(from.get());
            }
            if (to.isPresent()) {
                uri += "to=%s&".formatted(to.get());
            }

            return new RestTemplate().getForObject(uri, TrackerData[].class);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public TrackerData[] getLatestTrackersData(String userId) {
        try {
            var uri = trackerServiceUri + "/tracker-data/get-latest?userId=%s".formatted(userId);
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
            var req = new HttpEntity<>(body, headers);
            var tracker = new RestTemplate().postForObject(uri, req, Tracker.class);
            if (tracker != null) {
                Map<String, String> claims = new HashMap<>();
                claims.put(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.TRACKER.toString());
                return new TrackerRegisterResponse(tracker,
                        jwtHelper.createJwtForClaimsWithNoExpiry(tracker.id(), claims));
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Bad response from cnat-tracker-service");
        } catch (HttpStatusCodeException e) {
            var res = e.getResponseBodyAs(GenericResponse.class);
            throw new ResponseStatusException(e.getStatusCode(), res != null ? res.message() : null);
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
