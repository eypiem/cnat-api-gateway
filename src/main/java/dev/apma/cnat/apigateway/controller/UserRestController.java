package dev.apma.cnat.apigateway.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);

    @GetMapping("/hi")
    public String getUser(Authentication auth) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
        Map<String, Object> attributes = token.getTokenAttributes();
        LOGGER.info(attributes.toString());
        return "Hi, your are authenticated.";
    }

    @PostMapping("/register")
    public void register() {
        LOGGER.info("/register");
        //kafkaTemplate.send(KafkaTopics.TRACKER_STATUS_UPDATE_REQ, "New register request from api.");
    }

    @PostMapping("/delete")
    public void update() {
        LOGGER.info("/delete");
        //kafkaTemplate.send(KafkaTopics.TRACKER_STATUS_UPDATE_REQ, "New data update request from api.");
    }
}
