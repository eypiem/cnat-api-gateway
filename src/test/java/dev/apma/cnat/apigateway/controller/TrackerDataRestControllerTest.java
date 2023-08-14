package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.dto.Tracker;
import dev.apma.cnat.apigateway.dto.TrackerData;
import dev.apma.cnat.apigateway.service.JwtHelper;
import dev.apma.cnat.apigateway.service.TrackerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrackerDataRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtHelper jwtHelper;

    @MockBean
    private TrackerService trackerSvc;

    private String userJwt;

    private String trackerJwt;

    @BeforeEach
    void setup() {
        userJwt = jwtHelper.createJwtForClaims("1@test.com",
                Map.of(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.USER.toString()));
        trackerJwt =
                jwtHelper.createJwtForClaims("1", Map.of(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.TRACKER.toString()));
    }

    @Test
    public void getLatestTrackerData_Authorized() throws Exception {
        var t1 = new Tracker("1", "1@test.com", "name1");
        var td1 = new TrackerData(t1, Map.of("param1", 100), Instant.parse("2023-01-01T00:00:00.0Z"));
        var td2 = new TrackerData(t1, Map.of("param1", 80), Instant.parse("2023-01-01T00:00:00.1Z"));

        when(trackerSvc.getLatestTrackersData("1@test.com")).thenReturn(new TrackerData[]{td1, td2});
        mockMvc.perform(get("/tracker-data/get-latest").header("Authorization", "Bearer " + userJwt))
                //                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tracker.id").value(t1.id()))
                .andExpect(jsonPath("$[0].data.param1").value(td1.data().get("param1")))
                .andExpect(jsonPath("$[1].tracker.id").value(t1.id()))
                .andExpect(jsonPath("$[1].data.param1").value(td2.data().get("param1")));
    }

    @Test
    public void getLatestTrackerData_Unauthorized_1() throws Exception {
        mockMvc.perform(get("/tracker-data/get-latest")).andExpect(status().isUnauthorized());
    }

    @Test
    public void getLatestTrackerData_Unauthorized_2() throws Exception {
        mockMvc.perform(get("/tracker-data/get-latest").header("Authorization", "Bearer " + trackerJwt))
                .andExpect(status().isUnauthorized());
    }
}