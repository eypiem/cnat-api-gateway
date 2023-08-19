package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.dto.TrackerDataDTO;
import dev.apma.cnat.apigateway.request.TrackerDataRegisterRequest;
import dev.apma.cnat.apigateway.request.TrackerRegisterRequest;
import dev.apma.cnat.apigateway.response.*;
import dev.apma.cnat.apigateway.service.JwtHelper;
import dev.apma.cnat.apigateway.service.TrackerService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/trackers")
public class TrackerRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerRestController.class);

    private final TrackerService trackerSvc;

    private final JwtHelper jwtHelper;

    private final KafkaTemplate<String, TrackerDataDTO> kafkaTemplate;

    private final String trackerDataRegisterTopic;

    @Autowired
    public TrackerRestController(TrackerService trackerSvc,
                                 JwtHelper jwtHelper,
                                 KafkaTemplate<String, TrackerDataDTO> kafkaTemplate,
                                 @Value("${app.kafka.topics.tracker-data-register}") String trackerDataRegisterTopic) {
        this.trackerSvc = trackerSvc;
        this.jwtHelper = jwtHelper;
        this.kafkaTemplate = kafkaTemplate;
        this.trackerDataRegisterTopic = trackerDataRegisterTopic;
    }

    @Operation(description = "Register a new tracker for a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @PostMapping("")
    public TrackerRegisterResponse register(Authentication auth, @RequestBody TrackerRegisterRequest trr) {
        LOGGER.info("post /trackers");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            var tracker = trackerSvc.registerTracker(new TrackerDTO(null, subject, trr.name()));

            if (tracker != null) {
                var claims = Map.of(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.TRACKER.toString());
                return new TrackerRegisterResponse(tracker,
                        jwtHelper.createJwtForClaimsWithNoExpiry(tracker.id(), claims));
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Bad response from cnat-tracker-service");
        });
    }

    @Operation(description = "Delete a tracker for a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @DeleteMapping("/{trackerId}")
    public void deleteUserTracker(Authentication auth, @PathVariable String trackerId) {
        LOGGER.info("delete /trackers/{}", trackerId);

        JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            trackerSvc.onTrackerMatchUserOrElseThrow(trackerId, subject, () -> {
                trackerSvc.deleteTrackerById(trackerId);
            });
        });
    }

    @Operation(description = "Retrieve user's trackers")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("")
    public TrackersGetResponse getUserTrackers(Authentication auth) {
        LOGGER.info("get /trackers");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return TrackersGetResponse.fromTrackerDTOs(List.of(trackerSvc.getUserTrackers(subject)));
        });
    }

    @Operation(description = "Retrieve a tracker for a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/{trackerId}")
    public TrackerGetResponse getTracker(Authentication auth, @PathVariable String trackerId) {
        LOGGER.info("get /trackers/{}", trackerId);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return new TrackerGetResponse(trackerSvc.getTrackerById(trackerId));
        });
    }

    @Operation(description = "Register a new tracker data for a tracker")
    @PostMapping("/data")
    public void register(Authentication auth, @RequestBody TrackerDataRegisterRequest tdrr) {
        LOGGER.info("post /trackers/data: {}", tdrr);

        JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.TRACKER, (subject) -> {
            var req = new TrackerDataDTO(new TrackerDTO(subject, null, null), tdrr.data(), tdrr.timestamp());
            kafkaTemplate.send(trackerDataRegisterTopic, req);
            /// TODO: check if Kafka was successful
        });
    }

    @Operation(description = "Retrieve a tracker data for a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/{trackerId}/data")
    public TrackerDataGetResponse getTrackerData(Authentication auth,
                                                 @PathVariable String trackerId,
                                                 @RequestParam Optional<Instant> from,
                                                 @RequestParam Optional<Instant> to,
                                                 @RequestParam Optional<Boolean> hasLocation) {
        LOGGER.info("get /trackers/{}/data from: {} to: {}", trackerId, from, to);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerSvc.onTrackerMatchUserOrElseThrow(trackerId, subject, () -> {
                var td = List.of(trackerSvc.getTrackerData(trackerId, from, to, hasLocation));
                if (td.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tracker data available");
                }
                return TrackerDataGetResponse.fromTrackerDataDTOs(td);
            });
        });
    }

    @Operation(description = "Retrieve the latest data of each of the user's trackers")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/data/latest")
    public LatestTrackerDataGetResponse getLatestTrackersData(Authentication auth) {
        LOGGER.info("get /trackers/data/latest");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return LatestTrackerDataGetResponse.fromTrackerDataDTOs(List.of(trackerSvc.getLatestTrackersData(subject)));
        });
    }
}
