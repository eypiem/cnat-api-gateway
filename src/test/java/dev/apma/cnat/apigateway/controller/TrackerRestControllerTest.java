package dev.apma.cnat.apigateway.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.apma.cnat.apigateway.dto.Tracker;
import dev.apma.cnat.apigateway.dto.TrackerData;
import dev.apma.cnat.apigateway.response.TrackerRegisterResponse;
import dev.apma.cnat.apigateway.service.JwtHelper;
import dev.apma.cnat.apigateway.service.TrackerService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrackerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtHelper jwtHelper;

    @MockBean
    private TrackerService trackerSvc;

    private String userJwt;

    private String trackerJwt;

    private Tracker t1;
    private TrackerData td1;
    private TrackerData td2;

    @BeforeAll
    void setup() {
        t1 = new Tracker("1", "1@test.com", "name1");
        td1 = new TrackerData(t1, Map.of("param1", 100), Instant.parse("2023-01-01T00:00:00.0Z"));
        td2 = new TrackerData(t1, Map.of("param1", 80), Instant.parse("2023-01-01T00:00:00.1Z"));

        userJwt = jwtHelper.createJwtForClaims(t1.userId(),
                Map.of(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.USER.toString()));
        trackerJwt = jwtHelper.createJwtForClaims(t1.id(),
                Map.of(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.TRACKER.toString()));

    }

    @AfterAll
    void tearDown() {
        userJwt = null;
        trackerJwt = null;
        t1 = null;
        td1 = null;
        td2 = null;
    }

    @Test
    public void register_authorized() throws Exception {
        when(trackerSvc.registerTracker(isA(Tracker.class))).thenReturn(new TrackerRegisterResponse(t1, trackerJwt));

        mockMvc.perform(post("/trackers").header("Authorization", "Bearer " + userJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(t1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("tracker.id").value(t1.id()))
                .andExpect(jsonPath("accessToken").value(trackerJwt));
    }

    @Test
    public void register_unauthorized_1() throws Exception {
        when(trackerSvc.registerTracker(isA(Tracker.class))).thenReturn(new TrackerRegisterResponse(t1, trackerJwt));

        mockMvc.perform(post("/trackers").contentType(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(t1))).andExpect(status().isUnauthorized());
    }

    @Test
    public void register_unauthorized_2() throws Exception {
        when(trackerSvc.registerTracker(isA(Tracker.class))).thenReturn(new TrackerRegisterResponse(t1, trackerJwt));

        mockMvc.perform(post("/trackers").header("Authorization", "Bearer " + trackerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(t1))).andExpect(status().isUnauthorized());
    }

    @Test
    public void getLatestTrackerData_authorized() throws Exception {
        when(trackerSvc.getLatestTrackersData(t1.userId())).thenReturn(new TrackerData[]{td1, td2});

        mockMvc.perform(get("/trackers/data/latest").header("Authorization", "Bearer " + userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tracker.id").value(t1.id()))
                .andExpect(jsonPath("$[0].data.param1").value(td1.data().get("param1")))
                .andExpect(jsonPath("$[1].tracker.id").value(t1.id()))
                .andExpect(jsonPath("$[1].data.param1").value(td2.data().get("param1")));
    }

    @Test
    public void getLatestTrackerData_unauthorized_1() throws Exception {
        when(trackerSvc.getLatestTrackersData(t1.userId())).thenReturn(new TrackerData[]{td1, td2});

        mockMvc.perform(get("/trackers/data/latest")).andExpect(status().isUnauthorized());
    }

    @Test
    public void getLatestTrackerData_unauthorized_2() throws Exception {
        when(trackerSvc.getLatestTrackersData(t1.userId())).thenReturn(new TrackerData[]{td1, td2});

        mockMvc.perform(get("/trackers/data/latest").header("Authorization", "Bearer " + trackerJwt))
                .andExpect(status().isUnauthorized());
    }
}