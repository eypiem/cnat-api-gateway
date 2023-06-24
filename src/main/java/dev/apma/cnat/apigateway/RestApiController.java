package dev.apma.cnat.apigateway;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
public class RestApiController {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public RestApiController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to CNAT API!";
    }

    @PostMapping("/register")
    public void register() {
        System.out.println("new register request");
        kafkaTemplate.send(KafkaTopics.TRACKER_STATUS_UPDATE_REQ, "New register request from api.");
    }

    @PostMapping("/update")
    public void update() {
        System.out.println("new update request");
        kafkaTemplate.send(KafkaTopics.TRACKER_STATUS_UPDATE_REQ, "New data update request from api.");
    }
}
