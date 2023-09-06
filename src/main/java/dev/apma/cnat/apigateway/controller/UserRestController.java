package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.exception.CNATServiceException;
import dev.apma.cnat.apigateway.exception.FieldValidationException;
import dev.apma.cnat.apigateway.exception.userservice.UserServiceException;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserDeleteRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import dev.apma.cnat.apigateway.service.TrackerService;
import dev.apma.cnat.apigateway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * This class is a controller for REST API endpoints related to users.
 *
 * @author Amir Parsa Mahdian
 */
@RestController
@RequestMapping("/users")
public class UserRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);

    private final TrackerService trackerSvc;

    private final UserService userSvc;

    @Autowired
    public UserRestController(TrackerService trackerSvc, UserService userSvc) {
        this.trackerSvc = trackerSvc;
        this.userSvc = userSvc;
    }

    @Operation(description = "Register a new user")
    @PostMapping("")
    public void register(@Valid @RequestBody UserRegisterRequest urr) throws FieldValidationException,
            UserServiceException {
        LOGGER.info("post /users: {}", urr);
        userSvc.register(urr);
    }

    @Operation(description = "Authenticate a user")
    @PostMapping("/auth")
    public UserAuthResponse auth(@Valid @RequestBody UserAuthRequest uar) throws FieldValidationException,
            UserServiceException {
        LOGGER.info("post /users/auth: {}", uar);
        return userSvc.auth(uar);
    }

    @Operation(description = "Delete a user and all trackers and tracker data associated with them")
    @DeleteMapping("")
    public void delete(@Valid @RequestBody UserDeleteRequest req) throws CNATServiceException {
        LOGGER.info("delete /users");
        userSvc.deleteUser(req);
        trackerSvc.deleteAllUserTrackers(req.email());
    }
}
