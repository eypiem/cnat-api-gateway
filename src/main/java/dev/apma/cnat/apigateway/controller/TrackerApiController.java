package dev.apma.cnat.apigateway.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/tracker")
public class TrackerApiController {
    //private final static Logger LOGGER = LoggerFactory.getLogger(TrackerApiController.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public TrackerApiController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    @ResponseBody
    public String index() {
        return "Welcome to CNAT tracker API!";
    }

    //    @PostMapping("/register")
    //    public void register() {
    //        System.out.println("new register request");
    //        kafkaTemplate.send(KafkaTopics.TRACKER_STATUS_UPDATE_REQ, "New register request from api.");
    //    }

    @PostMapping("/update")
    public void update() {
        System.out.println("new update request");
        kafkaTemplate.send("${app.kafka.topics.tracker-data-register}", "New data update request from api.");
    }
}
