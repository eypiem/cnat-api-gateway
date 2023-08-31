package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.dto.TrackerDataDTO;
import dev.apma.cnat.apigateway.dto.ValidationErrorsDTO;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.trackerservice.TrackerOwnershipMismatchException;
import dev.apma.cnat.apigateway.exception.trackerservice.TrackerServiceCommunicationException;
import dev.apma.cnat.apigateway.response.*;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@code TrackerService} interface.
 *
 * @author Amir Parsa Mahdian
 * @see dev.apma.cnat.apigateway.service.TrackerService
 */
@Service
public class TrackerServiceImpl implements TrackerService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerServiceImpl.class);

    /**
     * The Tracker Service Base REST API URI.
     */
    private final String trackerServiceUri;

    /**
     * The service used for issuing and validating JWTs.
     */
    private final JwtService jwtSvc;

    /**
     * The Kafka template used sending tracker data.
     */
    private final KafkaTemplate<String, TrackerDataDTO> kafkaTemplate;

    /**
     * The Kafka topic used for registering new tracker data.
     */
    private final String trackerDataRegisterTopic;

    @Autowired
    public TrackerServiceImpl(@Value("${app.cnat.tracker-service}") String trackerServiceUri,
                              JwtService jwtSvc,
                              KafkaTemplate<String, TrackerDataDTO> kafkaTemplate,
                              @Value("${app.kafka.topics.tracker-data-register}") String trackerDataRegisterTopic) {
        this.trackerServiceUri = trackerServiceUri;
        this.jwtSvc = jwtSvc;
        this.kafkaTemplate = kafkaTemplate;
        this.trackerDataRegisterTopic = trackerDataRegisterTopic;
    }

    @Override
    public TrackerRegisterResponse registerTracker(TrackerDTO trackerDTO) throws FieldValidationException,
            TrackerServiceCommunicationException {
        var uri = trackerServiceUri;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(trackerDTO, headers);

        try {
            var r = new RestTemplate().postForObject(uri, request, TrackerDTO.class);

            if (r != null) {
                var claims = Map.of(JwtService.ROLE_ATTRIBUTE, JwtService.Role.TRACKER.toString());
                return new TrackerRegisterResponse(r, jwtSvc.createJwtForClaimsWithNoExpiry(r.id(), claims));
            }
            throw new TrackerServiceCommunicationException(
                    "Received unexpected null response from CNAT Tracker Service.");
        } catch (HttpClientErrorException.BadRequest e) {
            throw new FieldValidationException(e.getResponseBodyAs(ValidationErrorsDTO.class));
        } catch (RestClientException e) {
            throw new TrackerServiceCommunicationException();
        }
    }

    @Override
    public TrackerGetResponse getTrackerById(String trackerId) throws TrackerServiceCommunicationException {
        var uri = trackerServiceUri + "/%s".formatted(trackerId);

        try {
            var r = new RestTemplate().getForObject(uri, TrackerDTO.class);
            return new TrackerGetResponse(r);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Access to non-existent tracker requested.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (RestClientException e) {
            throw new TrackerServiceCommunicationException();
        }
    }

    @Override
    public TrackersGetResponse getUserTrackers(String userId) throws TrackerServiceCommunicationException {
        var uri = trackerServiceUri + "?userId=%s".formatted(userId);

        try {
            var r = new RestTemplate().getForObject(uri, TrackerDTO[].class);
            return TrackersGetResponse.fromTrackerDTOs(r == null ? List.of() : Arrays.asList(r));
        } catch (RestClientException e) {
            throw new TrackerServiceCommunicationException();
        }
    }

    @Override
    public void checkTrackerBelongsToUser(String trackerId,
                                          String userId) throws TrackerServiceCommunicationException,
            TrackerOwnershipMismatchException {
        var res = getTrackerById(trackerId);
        if (!userId.equals(res.tracker().userId())) {
            LOGGER.warn("Non-matching user and tracker request detected.");
            throw new TrackerOwnershipMismatchException();
        }
    }

    @Override
    public void deleteTrackerById(String trackerId) throws TrackerServiceCommunicationException {
        var uri = trackerServiceUri + "/%s".formatted(trackerId);

        try {
            new RestTemplate().delete(uri);
        } catch (RestClientException e) {
            throw new TrackerServiceCommunicationException();
        }
    }

    @Override
    public void deleteAllUserTrackers(String userId) throws TrackerServiceCommunicationException {
        var uri = trackerServiceUri + "?userId=%s".formatted(userId);

        try {
            new RestTemplate().delete(uri);
        } catch (RestClientException e) {
            throw new TrackerServiceCommunicationException();
        }
    }

    @Override
    public void registerTrackerData(TrackerDataDTO td) {
        kafkaTemplate.send(trackerDataRegisterTopic, td).whenComplete((result, ex) -> {
            if (ex == null) {
                LOGGER.info("Tracker data sent over Kafka.");
            } else {
                LOGGER.warn("Unable to send tracker data over Kafka: " + ex.getMessage());
            }
        });
    }

    @Override
    public TrackerDataGetResponse getTrackerData(String trackerId,
                                                 @Nullable Instant from,
                                                 @Nullable Instant to,
                                                 @Nullable Boolean hasCoordinates,
                                                 @Nullable Integer limit) throws TrackerServiceCommunicationException {
        var uri = trackerServiceUri + "/%s/data?".formatted(trackerId);

        try {
            if (from != null) {
                uri += "from=%s&".formatted(from);
            }
            if (to != null) {
                uri += "to=%s&".formatted(to);
            }
            if (hasCoordinates != null) {
                uri += "hasCoordinates=%s&".formatted(hasCoordinates);
            }
            if (limit != null) {
                uri += "limit=%d&".formatted(limit);
            }
            var r = new RestTemplate().getForObject(uri, TrackerDataDTO[].class);
            return TrackerDataGetResponse.fromTrackerDataDTOs(r == null ? List.of() : Arrays.asList(r));
        } catch (RestClientException e) {
            throw new TrackerServiceCommunicationException();
        }
    }

    @Override
    public LatestTrackerDataGetResponse getLatestTrackersData(String userId) throws TrackerServiceCommunicationException {
        var uri = trackerServiceUri + "/data/latest?userId=%s".formatted(userId);

        try {
            var r = new RestTemplate().getForObject(uri, TrackerDataDTO[].class);
            return LatestTrackerDataGetResponse.fromTrackerDataDTOs(r == null ? List.of() : Arrays.asList(r));
        } catch (RestClientException e) {
            throw new TrackerServiceCommunicationException();
        }
    }
}
