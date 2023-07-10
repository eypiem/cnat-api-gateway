package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.TrackerService;
import dev.apma.cnat.apigateway.dto.Tracker;
import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/tracker")
public class TrackerRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrackerRestController.class);

    @Autowired
    private TrackerService trackerService;

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @PostMapping("/register")
    public TrackerRegisterResponse register(Authentication auth, @RequestBody Tracker body) {
        LOGGER.info("/tracker/register");
        if (body.name() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key name is required");
        }

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerService.registerTracker(new Tracker(null, subject, body.name()));
        });
    }

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @DeleteMapping("/delete/{trackerId}")
    public void deleteUserTracker(Authentication auth, @PathVariable String trackerId) {
        LOGGER.info("/tracker/delete/{}", trackerId);

        JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            if (!subject.equals(trackerService.getTrackerById(trackerId).userId())) {
                LOGGER.warn("Non matching user and tracker request detected.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            trackerService.deleteTrackerById(trackerId);
        });
    }

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @GetMapping("/get")
    public Tracker[] getUserTrackers(Authentication auth) {
        LOGGER.info("/tracker/get");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            return trackerService.getUserTrackers(subject);
        });
    }
}
