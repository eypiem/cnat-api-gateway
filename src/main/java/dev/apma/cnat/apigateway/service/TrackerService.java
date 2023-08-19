package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.dto.TrackerDataDTO;
import dev.apma.cnat.apigateway.response.GenericResponse;
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
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class TrackerService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerService.class);

    private final String trackerServiceUri;

    @Autowired
    public TrackerService(@Value("${app.cnat.tracker-service}") String trackerServiceUri) {
        this.trackerServiceUri = trackerServiceUri;
    }

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

    public TrackerDTO getTrackerById(String trackerId) {
        var uri = trackerServiceUri + "/%s".formatted(trackerId);
        try {
            return new RestTemplate().getForObject(uri, TrackerDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Access to non-existent tracker requested.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public TrackerDataDTO[] getTrackerData(String trackerId,
                                           Optional<Instant> from,
                                           Optional<Instant> to,
                                           Optional<Boolean> hasLocation) {
        var uri = trackerServiceUri + "/%s/data?".formatted(trackerId);
        try {
            if (from.isPresent()) {
                uri += "from=%s&".formatted(from.get());
            }
            if (to.isPresent()) {
                uri += "to=%s&".formatted(to.get());
            }
            if (hasLocation.isPresent()) {
                uri += "hasLocation=%s".formatted(hasLocation.get());
            }

            return new RestTemplate().getForObject(uri, TrackerDataDTO[].class);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public TrackerDataDTO[] getLatestTrackersData(String userId) {
        var uri = trackerServiceUri + "/data/latest?userId=%s".formatted(userId);
        try {
            return new RestTemplate().getForObject(uri, TrackerDataDTO[].class);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public void deleteTrackerById(String trackerId) {
        var uri = trackerServiceUri + "/%s".formatted(trackerId);
        try {
            new RestTemplate().delete(uri);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public TrackerDTO registerTracker(TrackerDTO trackerDTO) {
        var uri = trackerServiceUri;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            var req = new HttpEntity<>(trackerDTO, headers);
            return new RestTemplate().postForObject(uri, req, TrackerDTO.class);
        } catch (HttpStatusCodeException e) {
            var res = e.getResponseBodyAs(GenericResponse.class);
            throw new ResponseStatusException(e.getStatusCode(), res != null ? res.message() : null);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    public TrackerDTO[] getUserTrackers(String userId) {
        var uri = trackerServiceUri + "?userId=%s".formatted(userId);
        try {
            return new RestTemplate().getForObject(uri, TrackerDTO[].class);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }
}
