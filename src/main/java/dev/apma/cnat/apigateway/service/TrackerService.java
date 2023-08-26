package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.TrackerServiceException;
import dev.apma.cnat.apigateway.response.*;
import jakarta.annotation.Nullable;

import java.time.Instant;

public interface TrackerService {

    void checkTrackerBelongsToUser(String trackerId, String userId) throws TrackerServiceException;

    TrackerGetResponse getTrackerById(String trackerId) throws TrackerServiceException;

    TrackerDataGetResponse getTrackerData(String trackerId,
                                          @Nullable Instant from,
                                          @Nullable Instant to,
                                          @Nullable Boolean hasLocation,
                                          @Nullable Integer limit) throws TrackerServiceException;

    LatestTrackerDataGetResponse getLatestTrackersData(String userId) throws TrackerServiceException;

    void deleteTrackerById(String trackerId) throws TrackerServiceException;

    TrackerRegisterResponse registerTracker(TrackerDTO trackerDTO) throws FieldValidationException,
            TrackerServiceException;

    TrackersGetResponse getUserTrackers(String userId) throws TrackerServiceException;

    void deleteAllUserTrackers(String userId) throws TrackerServiceException;
}
