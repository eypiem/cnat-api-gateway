package dev.apma.cnat.apigateway.service;


import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.dto.TrackerDataDTO;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.trackerservice.TrackerOwnershipMismatchException;
import dev.apma.cnat.apigateway.exception.trackerservice.TrackerServiceCommunicationException;
import dev.apma.cnat.apigateway.response.*;
import jakarta.annotation.Nullable;

import java.time.Instant;

/**
 * This interface represents the CNAT Tracker Service.
 *
 * @author Amir Parsa Mahdian
 */
public interface TrackerService {

    /**
     * Registers a new tracker.
     *
     * @param trackerDTO tracker to be registered
     * @return A {@code TrackerRegisterResponse} containing the new tracker and an access token
     * @throws FieldValidationException             if CNAT Tracker Service returns a validation error
     * @throws TrackerServiceCommunicationException if an unexpected error occurs during communication with CNAT Tracker
     *                                              Service
     */
    TrackerRegisterResponse registerTracker(TrackerDTO trackerDTO) throws FieldValidationException,
            TrackerServiceCommunicationException;

    /**
     * Returns the tracker having the provided ID.
     *
     * @param trackerId the tracker's ID
     * @return the tracker having the provided ID
     * @throws TrackerServiceCommunicationException if an unexpected error occurs during communication with CNAT Tracker
     *                                              Service
     */
    TrackerGetResponse getTrackerById(String trackerId) throws TrackerServiceCommunicationException;

    /**
     * Returns all trackers associated with the provided userId.
     *
     * @param userId the user's ID to match trackers with
     * @return all trackers associated with the provided userId
     * @throws TrackerServiceCommunicationException if an unexpected error occurs during communication with CNAT Tracker
     *                                              Service
     */
    TrackersGetResponse getUserTrackers(String userId) throws TrackerServiceCommunicationException;

    /**
     * Checks if the tracker having the provided ID has the provided user ID. If the tracker belongs to the user, the
     * method does nothing.
     *
     * @param trackerId the tracker's ID
     * @param userId    the user's ID to match the tracker with
     * @throws TrackerServiceCommunicationException if an unexpected error occurs during communication with CNAT Tracker
     * @throws TrackerOwnershipMismatchException    If the tracker does not belong to the user
     */
    void checkTrackerBelongsToUser(String trackerId,
                                   String userId) throws TrackerServiceCommunicationException,
            TrackerOwnershipMismatchException;

    /**
     * Deletes the tracker and all tracker data associated with it.
     *
     * @param trackerId the tracker's ID
     * @throws TrackerServiceCommunicationException if an unexpected error occurs during communication with CNAT Tracker
     *                                              Service
     */
    void deleteTrackerById(String trackerId) throws TrackerServiceCommunicationException;

    /**
     * Deletes all the trackers associated with the provided userId and all tracker data associated with them.
     *
     * @param userId the user's ID to match trackers with
     * @throws TrackerServiceCommunicationException if an unexpected error occurs during communication with CNAT Tracker
     *                                              Service
     */
    void deleteAllUserTrackers(String userId) throws TrackerServiceCommunicationException;

    /**
     * Registers a new tacker data.
     *
     * @param req the TrackerDataDTO to register
     */
    void registerTrackerData(TrackerDataDTO req);

    /**
     * Returns all tracker's data satisfying the provided parameters.
     *
     * @param trackerId      the tracker's ID
     * @param from           filter tracker data with Instant after
     * @param to             filter tracker data with Instant before
     * @param hasCoordinates filter tracker data having coordinates
     * @param limit          limit the number of results (if null, a default limit may be applied)
     * @return all tracker's data satisfying the provided parameters
     * @throws TrackerServiceCommunicationException if an unexpected error occurs during communication with CNAT Tracker
     *                                              Service
     */
    TrackerDataGetResponse getTrackerData(String trackerId,
                                          @Nullable Instant from,
                                          @Nullable Instant to,
                                          @Nullable Boolean hasCoordinates,
                                          @Nullable Integer limit) throws TrackerServiceCommunicationException;

    /**
     * Returns the most recent tracker data from each of the trackers associated with the provided user ID.
     *
     * @param userId the user's ID to match trackers with
     * @return the latest tracker data of each the trackers associated with the provided user ID
     * @throws TrackerServiceCommunicationException if an unexpected error occurs during communication with CNAT Tracker
     *                                              Service
     */
    LatestTrackerDataGetResponse getLatestTrackersData(String userId) throws TrackerServiceCommunicationException;
}
