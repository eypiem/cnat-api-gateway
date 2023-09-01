package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.dto.TrackerDataDTO;
import dev.apma.cnat.apigateway.exception.CNATServiceException;
import dev.apma.cnat.apigateway.exception.JwtRoleMismatchException;
import dev.apma.cnat.apigateway.exception.trackerservice.TrackerOwnershipMismatchException;
import dev.apma.cnat.apigateway.request.TrackerDataRegisterRequest;
import dev.apma.cnat.apigateway.request.TrackerRegisterRequest;
import dev.apma.cnat.apigateway.response.*;
import dev.apma.cnat.apigateway.service.JwtService;
import dev.apma.cnat.apigateway.service.TrackerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

/**
 * This class is a controller for REST API endpoints related to trackers.
 *
 * @author Amir Parsa Mahdian
 */
@RestController
@RequestMapping("/trackers")
public class TrackerRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerRestController.class);

    private final TrackerService trackerSvc;

    @Autowired
    public TrackerRestController(TrackerService trackerSvc) {
        this.trackerSvc = trackerSvc;
    }

    @Operation(description = "Register a new tracker for a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @PostMapping("")
    public TrackerRegisterResponse registerTracker(Authentication auth,
                                                   @RequestBody @Valid TrackerRegisterRequest trr) throws CNATServiceException, JwtRoleMismatchException {
        LOGGER.info("post /trackers");

        var subject = JwtService.getSubjectForRole(auth, JwtService.Role.USER);
        return trackerSvc.registerTracker(new TrackerDTO(null, subject, trr.name()));
    }

    @Operation(description = "Delete a tracker for a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @DeleteMapping("/{trackerId}")
    public void deleteTracker(Authentication auth,
                              @PathVariable String trackerId) throws CNATServiceException, JwtRoleMismatchException {
        LOGGER.info("delete /trackers/{}", trackerId);

        var subject = JwtService.getSubjectForRole(auth, JwtService.Role.USER);
        var r = trackerSvc.getTrackerById(trackerId);
        if (TrackerService.trackerBelongsToUser(r.tracker(), subject)) {
            trackerSvc.deleteTrackerById(trackerId);
        }
        throw new TrackerOwnershipMismatchException();
    }

    @Operation(description = "Retrieve user's trackers")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("")
    public TrackersGetResponse getUserTrackers(Authentication auth) throws CNATServiceException,
            JwtRoleMismatchException {
        LOGGER.info("get /trackers");

        var subject = JwtService.getSubjectForRole(auth, JwtService.Role.USER);
        return trackerSvc.getUserTrackers(subject);
    }

    @Operation(description = "Retrieve a tracker for a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/{trackerId}")
    public TrackerGetResponse getTracker(Authentication auth,
                                         @PathVariable String trackerId) throws CNATServiceException,
            JwtRoleMismatchException {
        LOGGER.info("get /trackers/{}", trackerId);

        var subject = JwtService.getSubjectForRole(auth, JwtService.Role.USER);
        var r = trackerSvc.getTrackerById(trackerId);
        if (TrackerService.trackerBelongsToUser(r.tracker(), subject)) {
            return r;
        }
        throw new TrackerOwnershipMismatchException();
    }

    @Operation(description = "Register a new tracker data for a tracker")
    @PostMapping("/data")
    public void registerTrackerData(Authentication auth,
                                    @RequestBody @Valid TrackerDataRegisterRequest req) throws JwtRoleMismatchException {
        LOGGER.info("post /trackers/data: {}", req);

        var subject = JwtService.getSubjectForRole(auth, JwtService.Role.TRACKER);
        var td = new TrackerDataDTO(new TrackerDTO(subject, null, null), req.data(), req.timestamp());
        trackerSvc.registerTrackerData(td);
    }

    @Operation(description = "Retrieve a tracker data for a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/{trackerId}/data")
    public TrackerDataGetResponse getTrackerData(Authentication auth,
                                                 @PathVariable String trackerId,
                                                 @RequestParam Optional<Instant> from,
                                                 @RequestParam Optional<Instant> to,
                                                 @RequestParam Optional<Boolean> hasCoordinates,
                                                 @RequestParam Optional<Integer> limit) throws CNATServiceException,
            JwtRoleMismatchException {
        LOGGER.info("get /trackers/{}/data from: {} to: {}", trackerId, from, to);

        var subject = JwtService.getSubjectForRole(auth, JwtService.Role.USER);
        var r = trackerSvc.getTrackerById(trackerId);
        if (TrackerService.trackerBelongsToUser(r.tracker(), subject)) {
            return trackerSvc.getTrackerData(trackerId,
                    from.orElse(null),
                    to.orElse(null),
                    hasCoordinates.orElse(null),
                    limit.orElse(null));
        }
        throw new TrackerOwnershipMismatchException();
    }

    @Operation(description = "Retrieve the latest data of each of the user's trackers")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/data/latest")
    public LatestTrackerDataGetResponse getLatestTrackersData(Authentication auth) throws CNATServiceException,
            JwtRoleMismatchException {
        LOGGER.info("get /trackers/data/latest");

        var subject = JwtService.getSubjectForRole(auth, JwtService.Role.USER);
        return trackerSvc.getLatestTrackersData(subject);
    }
}
