package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import dev.apma.cnat.apigateway.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class JwtRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(JwtRestController.class);

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserService userSvc;

    @CrossOrigin(origins = "${app.cnat.web-app}")
    @PostMapping("login")
    public UserAuthResponse login(@RequestBody UserAuthRequest body) {
        if (userSvc.emailAndPasswordIsValid(body)) {
            LOGGER.info("User authentication succeeded");
            Map<String, String> claims = new HashMap<>();
            claims.put(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.USER.toString());
            return new UserAuthResponse(body.email(), jwtHelper.createJwtForClaims(body.email(), claims));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication failed");
        }
    }


}
