package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.TrackerServiceException;
import dev.apma.cnat.apigateway.response.*;

import java.time.Instant;
import java.util.Optional;

public interface TrackerService {

    void checkTrackerBelongsToUser(String trackerId, String userId) throws TrackerServiceException;

    TrackerGetResponse getTrackerById(String trackerId) throws TrackerServiceException;

    TrackerDataGetResponse getTrackerData(String trackerId,
                                          Optional<Instant> from,
                                          Optional<Instant> to,
                                          Optional<Boolean> hasLocation) throws TrackerServiceException;

    LatestTrackerDataGetResponse getLatestTrackersData(String userId) throws TrackerServiceException;

    void deleteTrackerById(String trackerId) throws TrackerServiceException;

    TrackerRegisterResponse registerTracker(TrackerDTO trackerDTO) throws FieldValidationException,
            TrackerServiceException;

    TrackersGetResponse getUserTrackers(String userId) throws TrackerServiceException;

    void deleteAllUserTrackers(String userId) throws TrackerServiceException;
}
