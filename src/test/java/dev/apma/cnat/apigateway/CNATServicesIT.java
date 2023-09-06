package dev.apma.cnat.apigateway;


import dev.apma.cnat.apigateway.request.TrackerRegisterRequest;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserDeleteRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.TrackerDataGetResponse;
import dev.apma.cnat.apigateway.response.TrackerGetResponse;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CNATServicesIT {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static String BASE_URL;

    @BeforeAll
    void setup() {
        BASE_URL = "http://localhost:" + port;
    }

    @Test
    public void user_should_be_able_to_register() {
        String userEmail = UUID.randomUUID() + "@test.com";
        String userPassword = "12345678";

        registerUser(new UserRegisterRequest(userEmail, userPassword, "fn", "ln"));
    }

    @Test
    public void user_should_be_able_to_authenticate() {
        String userEmail = UUID.randomUUID() + "@test.com";
        String userPassword = "12345678";

        registerUser(new UserRegisterRequest(userEmail, userPassword, "fn", "ln"));
        authUser(new UserAuthRequest(userEmail, userPassword));
    }

    @Test
    public void user_should_be_able_to_register_tracker() {
        String userEmail = UUID.randomUUID() + "@test.com";
        String userPassword = "12345678";

        registerUser(new UserRegisterRequest(userEmail, userPassword, "fn", "ln"));
        var userJwt = authUser(new UserAuthRequest(userEmail, userPassword)).accessToken();
        registerTracker(new TrackerRegisterRequest("trackerName1"), userJwt);
    }

    @Test
    public void user_should_be_able_to_get_tracker() {
        String userEmail = UUID.randomUUID() + "@test.com";
        String userPassword = "12345678";

        registerUser(new UserRegisterRequest(userEmail, userPassword, "fn", "ln"));
        var userJwt = authUser(new UserAuthRequest(userEmail, userPassword)).accessToken();
        var trr = registerTracker(new TrackerRegisterRequest("trackerName1"), userJwt);
        getTracker(trr.tracker().id(), userJwt);
    }

    @Test
    public void user_should_be_able_to_get_tracker_data() {
        String userEmail = UUID.randomUUID() + "@test.com";
        String userPassword = "12345678";

        registerUser(new UserRegisterRequest(userEmail, userPassword, "fn", "ln"));
        var userJwt = authUser(new UserAuthRequest(userEmail, userPassword)).accessToken();
        var trr = registerTracker(new TrackerRegisterRequest("trackerName1"), userJwt);
        getTrackerData(trr.tracker().id(), userJwt);
    }

    @Test
    public void user_should_be_able_to_delete_tracker() {
        String userEmail = UUID.randomUUID() + "@test.com";
        String userPassword = "12345678";

        registerUser(new UserRegisterRequest(userEmail, userPassword, "fn", "ln"));
        var userJwt = authUser(new UserAuthRequest(userEmail, userPassword)).accessToken();
        var trr = registerTracker(new TrackerRegisterRequest("trackerName1"), userJwt);
        deleteTracker(trr.tracker().id(), userJwt);
        getNonExistentTracker(trr.tracker().id(), userJwt);
    }

    @Test
    public void user_should_be_able_to_delete_their_account() {
        String userEmail = UUID.randomUUID() + "@test.com";
        String userPassword = "12345678";

        registerUser(new UserRegisterRequest(userEmail, userPassword, "fn", "ln"));
        deleteUser(new UserDeleteRequest(userEmail, userPassword));
    }

    private void registerUser(UserRegisterRequest req) {
        Assertions.assertEquals(HttpStatus.OK,
                restTemplate.postForEntity(BASE_URL + "/users", req, Object.class).getStatusCode());
    }

    private UserAuthResponse authUser(UserAuthRequest req) {
        var res = restTemplate.postForEntity(BASE_URL + "/users/auth", req, UserAuthResponse.class);
        Assertions.assertEquals(HttpStatus.OK, res.getStatusCode());
        Assertions.assertNotNull(res.getBody());
        Assertions.assertNotNull(res.getBody().accessToken());
        return res.getBody();
    }

    private void deleteUser(UserDeleteRequest req) {
        Assertions.assertEquals(HttpStatus.OK,
                restTemplate.exchange(BASE_URL + "/users", HttpMethod.DELETE, new HttpEntity<>(req), String.class)
                        .getStatusCode());
    }

    private TrackerRegisterResponse registerTracker(TrackerRegisterRequest req, String userJwt) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userJwt);
        var res = restTemplate.exchange(BASE_URL + "/trackers",
                HttpMethod.POST,
                new HttpEntity<>(req, headers),
                TrackerRegisterResponse.class);
        Assertions.assertEquals(HttpStatus.OK, res.getStatusCode());
        Assertions.assertNotNull(res.getBody());
        Assertions.assertNotNull(res.getBody().tracker().id());
        Assertions.assertNotNull(res.getBody().accessToken());
        return res.getBody();
    }

    private TrackerGetResponse getTracker(String trackerId, String userJwt) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userJwt);
        var res = restTemplate.exchange(BASE_URL + "/trackers/" + trackerId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TrackerGetResponse.class);
        Assertions.assertEquals(HttpStatus.OK, res.getStatusCode());
        Assertions.assertNotNull(res.getBody());
        return res.getBody();
    }

    private TrackerDataGetResponse getTrackerData(String trackerId, String userJwt) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userJwt);
        var res = restTemplate.exchange(BASE_URL + "/trackers/" + trackerId + "/data",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TrackerDataGetResponse.class);
        Assertions.assertEquals(HttpStatus.OK, res.getStatusCode());
        Assertions.assertNotNull(res.getBody());
        return res.getBody();
    }

    private void getNonExistentTracker(String trackerId, String userJwt) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userJwt);
        var res = restTemplate.exchange(BASE_URL + "/trackers/" + trackerId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                TrackerGetResponse.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    private void deleteTracker(String trackerId, String userJwt) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userJwt);
        var res = restTemplate.exchange(BASE_URL + "/trackers/" + trackerId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                TrackerRegisterResponse.class);
        Assertions.assertEquals(HttpStatus.OK, res.getStatusCode());
    }
}
