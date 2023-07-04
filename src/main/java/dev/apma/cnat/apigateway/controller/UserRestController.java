package dev.apma.cnat.apigateway.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);

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
