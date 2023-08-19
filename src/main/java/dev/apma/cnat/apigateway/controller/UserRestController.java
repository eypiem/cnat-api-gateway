package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.GenericResponse;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import dev.apma.cnat.apigateway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userSvc;

    @Autowired
    public UserRestController(UserService userSvc) {
        this.userSvc = userSvc;
    }

    @Operation(description = "Register a new user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @PostMapping("")
    public GenericResponse register(@Valid @RequestBody UserRegisterRequest urr) {
        LOGGER.info("post /users: {}", urr);
        return userSvc.register(urr);
    }

    @Operation(description = "Authenticate a user")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @PostMapping("/auth")
    public UserAuthResponse auth(@Valid @RequestBody UserAuthRequest uar) {
        LOGGER.info("post /users/auth: {}", uar);
        return userSvc.auth(uar);
    }

    @Operation(description = "Delete a user and all trackers and tracker data associated with them")
    @CrossOrigin(origins = "${app.cnat.web-app}")
    @DeleteMapping("")
    public void delete(Authentication auth) {
        LOGGER.info("delete /users");
        //        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
        //            /// TODO: Implement
        //            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        //        });
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not yet implemented");
    }
}
