package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.TrackerData;
import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.TrackerDataRegisterRequest;
import dev.apma.cnat.apigateway.response.GenericResponse;
import dev.apma.cnat.apigateway.service.TrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping(path = "/tracker-data")
public class TrackerDataRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerDataRestController.class);

    @Autowired
    private TrackerService trackerSvc;

    @Autowired
    private KafkaTemplate<String, TrackerDataRegisterRequest> kafkaTemplate;

    @Value("${app.kafka.topics.tracker-data-register}")
    private String trackerDataRegisterTopic;

    @PostMapping("/register")
    public GenericResponse register(Authentication auth, @RequestBody TrackerDataRegisterRequest body) {
        LOGGER.info("/tracker-data/register: {}", body);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.TRACKER, (subject) -> {
            var req = new TrackerDataRegisterRequest(subject, body.data(), body.timestamp());
            kafkaTemplate.send(trackerDataRegisterTopic, req);
            /// TODO: check if Kafka was successful
            return new GenericResponse("OK");
        });
    }

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/get/{trackerId}")
    public TrackerData[] getTrackerData(Authentication auth,
                                        @PathVariable String trackerId,
                                        @RequestParam Optional<Instant> from,
                                        @RequestParam Optional<Instant> to) {
        LOGGER.info("/tracker-data/get/{} from: {} to: {}", trackerId, from, to);

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerSvc.onTrackerMatchUserOrElseThrow(trackerId, subject, () -> {
                return trackerSvc.getTrackerData(trackerId, from, to);
            });
        });
    }

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/get-latest")
    public TrackerData[] getLatestTrackersData(Authentication auth) {
        LOGGER.info("/tracker-data/get-latest");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerSvc.getLatestTrackersData(subject);
        });
    }
}
