package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.dto.TrackerDataDTO;
import dev.apma.cnat.apigateway.dto.ValidationErrorsDTO;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.TrackerOwnershipMismatchException;
import dev.apma.cnat.apigateway.exception.TrackerServiceException;
import dev.apma.cnat.apigateway.response.*;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TrackerServiceImpl implements TrackerService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerServiceImpl.class);

    private final String trackerServiceUri;

    private final JwtHelper jwtHelper;

    @Autowired
    public TrackerServiceImpl(@Value("${app.cnat.tracker-service}") String trackerServiceUri, JwtHelper jwtHelper) {
        this.trackerServiceUri = trackerServiceUri;
        this.jwtHelper = jwtHelper;
    }

    public void checkTrackerBelongsToUser(String trackerId, String userId) throws TrackerServiceException {
        var res = getTrackerById(trackerId);
        if (!userId.equals(res.tracker().userId())) {
            LOGGER.warn("Non-matching user and tracker request detected.");
            throw new TrackerOwnershipMismatchException();
        }
    }

    @Override
    public TrackerGetResponse getTrackerById(String trackerId) throws TrackerServiceException {
        var uri = trackerServiceUri + "/%s".formatted(trackerId);

        try {
            var r = new RestTemplate().getForObject(uri, TrackerDTO.class);
            return new TrackerGetResponse(r);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Access to non-existent tracker requested.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (RestClientException e) {
            throw new TrackerServiceException("Error in communicating with cnat-tracker-service");
        }
    }

    @Override
    public TrackerDataGetResponse getTrackerData(String trackerId,
                                                 Optional<Instant> from,
                                                 Optional<Instant> to,
                                                 Optional<Boolean> hasLocation) throws TrackerServiceException {
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
            var r = new RestTemplate().getForObject(uri, TrackerDataDTO[].class);
            return TrackerDataGetResponse.fromTrackerDataDTOs(r == null ? List.of() : Arrays.asList(r));
        } catch (RestClientException e) {
            throw new TrackerServiceException("Error in communicating with cnat-tracker-service");
        }
    }

    @Override
    public LatestTrackerDataGetResponse getLatestTrackersData(String userId) throws TrackerServiceException {
        var uri = trackerServiceUri + "/data/latest?userId=%s".formatted(userId);

        try {
            var r = new RestTemplate().getForObject(uri, TrackerDataDTO[].class);
            return LatestTrackerDataGetResponse.fromTrackerDataDTOs(r == null ? List.of() : Arrays.asList(r));
        } catch (RestClientException e) {
            throw new TrackerServiceException("Error in communicating with cnat-tracker-service");
        }
    }

    @Override
    public void deleteTrackerById(String trackerId) throws TrackerServiceException {
        var uri = trackerServiceUri + "/%s".formatted(trackerId);

        try {
            new RestTemplate().delete(uri);
        } catch (RestClientException e) {
            throw new TrackerServiceException("Error in communicating with cnat-tracker-service");
        }
    }

    @Override
    public TrackerRegisterResponse registerTracker(TrackerDTO trackerDTO) throws FieldValidationException,
            TrackerServiceException {
        var uri = trackerServiceUri;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(trackerDTO, headers);

        try {
            var r = new RestTemplate().postForObject(uri, request, TrackerDTO.class);

            if (r != null) {
                var claims = Map.of(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.TRACKER.toString());
                return new TrackerRegisterResponse(r, jwtHelper.createJwtForClaimsWithNoExpiry(r.id(), claims));
            }
            throw new TrackerServiceException("Error in communicating with cnat-tracker-service");
        } catch (HttpClientErrorException.BadRequest e) {
            throw new FieldValidationException(e.getResponseBodyAs(ValidationErrorsDTO.class));
        } catch (RestClientException e) {
            throw new TrackerServiceException("Error in communicating with cnat-tracker-service");
        }
    }

    @Override
    public TrackersGetResponse getUserTrackers(String userId) throws TrackerServiceException {
        var uri = trackerServiceUri + "?userId=%s".formatted(userId);

        try {
            var r = new RestTemplate().getForObject(uri, TrackerDTO[].class);
            return TrackersGetResponse.fromTrackerDTOs(r == null ? List.of() : Arrays.asList(r));
        } catch (RestClientException e) {
            throw new TrackerServiceException("Error in communicating with cnat-tracker-service");
        }
    }

    @Override
    public void deleteAllUserTrackers(String userId) throws TrackerServiceException {
        var uri = trackerServiceUri + "?userId=%s".formatted(userId);

        try {
            new RestTemplate().delete(uri);
        } catch (RestClientException e) {
            throw new TrackerServiceException("Error in communicating with cnat-tracker-service");
        }
    }
}
