package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.Tracker;
import dev.apma.cnat.apigateway.dto.TrackerData;
import dev.apma.cnat.apigateway.request.TrackerDataRegisterRequest;
import dev.apma.cnat.apigateway.response.GenericResponse;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import dev.apma.cnat.apigateway.service.JwtHelper;
import dev.apma.cnat.apigateway.service.TrackerService;
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
import java.util.Optional;

@RestController
@RequestMapping("/trackers")
public class TrackerRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerRestController.class);

    private final TrackerService trackerSvc;

    private final KafkaTemplate<String, TrackerDataRegisterRequest> kafkaTemplate;

    private final String trackerDataRegisterTopic;

    @Autowired
    public TrackerRestController(TrackerService trackerSvc,
                                 KafkaTemplate<String, TrackerDataRegisterRequest> kafkaTemplate,
                                 @Value("${app.kafka.topics.tracker-data-register}") String trackerDataRegisterTopic) {
        this.trackerSvc = trackerSvc;
        this.kafkaTemplate = kafkaTemplate;
        this.trackerDataRegisterTopic = trackerDataRegisterTopic;
    }

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @PostMapping("")
    public TrackerRegisterResponse register(Authentication auth, @RequestBody Tracker body) {
        LOGGER.info("post /trackers");
        if (body.name() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key name is required");
        }

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerSvc.registerTracker(new Tracker(null, subject, body.name()));
        });
    }

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

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("")
    public Tracker[] getUserTrackers(Authentication auth) {
        LOGGER.info("get /trackers");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerSvc.getUserTrackers(subject);
        });
    }

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/{trackerId}")
    public Tracker getTracker(Authentication auth, @PathVariable String trackerId) {
        LOGGER.info("get /trackers/{}", trackerId);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerSvc.getTrackerById(trackerId);
        });
    }


    @PostMapping("/data")
    public GenericResponse register(Authentication auth, @RequestBody TrackerDataRegisterRequest body) {
        LOGGER.info("post /trackers/data: {}", body);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.TRACKER, (subject) -> {
            var req = new TrackerDataRegisterRequest(subject, body.data(), body.timestamp());
            kafkaTemplate.send(trackerDataRegisterTopic, req);
            /// TODO: check if Kafka was successful
            return new GenericResponse("OK");
        });
    }

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/{trackerId}/data")
    public TrackerData[] getTrackerData(Authentication auth,
                                        @PathVariable String trackerId,
                                        @RequestParam Optional<Instant> from,
                                        @RequestParam Optional<Instant> to,
                                        @RequestParam Optional<Boolean> hasLocation) {
        LOGGER.info("get /trackers/{}/data from: {} to: {}", trackerId, from, to);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerSvc.onTrackerMatchUserOrElseThrow(trackerId, subject, () -> {
                return trackerSvc.getTrackerData(trackerId, from, to, hasLocation);
            });
        });
    }

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/data/latest")
    public TrackerData[] getLatestTrackersData(Authentication auth) {
        LOGGER.info("get /trackers/data/latest");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerSvc.getLatestTrackersData(subject);
        });
    }
}
