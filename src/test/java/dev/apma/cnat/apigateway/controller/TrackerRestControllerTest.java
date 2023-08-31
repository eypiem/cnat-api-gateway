package dev.apma.cnat.apigateway.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.apma.cnat.apigateway.dto.TrackerDTO;
import dev.apma.cnat.apigateway.dto.TrackerDataDTO;
import dev.apma.cnat.apigateway.request.TrackerDataRegisterRequest;
import dev.apma.cnat.apigateway.request.TrackerRegisterRequest;
import dev.apma.cnat.apigateway.response.*;
import dev.apma.cnat.apigateway.service.JwtService;
import dev.apma.cnat.apigateway.service.TrackerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrackerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private static TrackerService trackerSvc;

    private static String userJwt;

    private static String trackerJwt;

    private static TrackerDTO t1;
    private static TrackerDataDTO td1;
    private static TrackerDataDTO td2;

    @BeforeAll
    void setup() {
        t1 = new TrackerDTO("1", "1@test.com", "name1");
        td1 = new TrackerDataDTO(t1, Map.of("param1", 100), Instant.parse("2023-01-01T00:00:00.0Z"));
        td2 = new TrackerDataDTO(t1, Map.of("param1", 80), Instant.parse("2023-01-01T00:00:00.1Z"));

        userJwt = jwtService.createJwtForClaims(t1.userId(),
                Map.of(JwtService.ROLE_ATTRIBUTE, JwtService.Role.USER.toString()));
        trackerJwt = jwtService.createJwtForClaims(t1.id(),
                Map.of(JwtService.ROLE_ATTRIBUTE, JwtService.Role.TRACKER.toString()));

    }

    @AfterAll
    void tearDown() {
        userJwt = null;
        trackerJwt = null;
        t1 = null;
        td1 = null;
        td2 = null;
    }

    @Nested
    class RegisterTracker {

        @BeforeAll
        static void setup() throws Exception {
            when(trackerSvc.registerTracker(isA(TrackerDTO.class))).thenReturn(new TrackerRegisterResponse(t1,
                    trackerJwt));
        }

        @Test
        void valid() throws Exception {
            var trr = new TrackerRegisterRequest(t1.name());

            mockMvc.perform(post("/trackers").header("Authorization", "Bearer " + userJwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(trr)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("tracker.id").value(t1.id()))
                    .andExpect(jsonPath("accessToken").isNotEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void invalid_name(String name) throws Exception {
            var trr = new TrackerRegisterRequest(name);

            mockMvc.perform(post("/trackers").header("Authorization", "Bearer " + userJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(trr))).andExpect(status().isBadRequest());
        }

        @Test
        public void unauthorized_no_jwt() throws Exception {
            var trr = new TrackerRegisterRequest(t1.name());

            mockMvc.perform(post("/trackers").contentType(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(trr))).andExpect(status().isUnauthorized());
        }

        @Test
        public void unauthorized_tracker_jwt() throws Exception {
            var trr = new TrackerRegisterRequest(t1.name());

            mockMvc.perform(post("/trackers").header("Authorization", "Bearer " + trackerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(trr))).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteTracker {

        @BeforeAll
        static void setup() throws Exception {
            doNothing().when(trackerSvc).deleteTrackerById(t1.id());
        }

        @Test
        public void valid() throws Exception {
            mockMvc.perform(delete("/trackers/%s".formatted(t1.id())).header("Authorization", "Bearer " + userJwt))
                    .andExpect(status().isOk());
        }

        @Test
        public void unauthorized_no_jwt() throws Exception {
            mockMvc.perform(delete("/trackers/%s".formatted(t1.id()))).andExpect(status().isUnauthorized());
        }

        @Test
        public void unauthorized_tracker_jwt() throws Exception {
            mockMvc.perform(delete("/trackers/%s".formatted(t1.id())).header("Authorization", "Bearer " + trackerJwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetUserTrackers {

        @BeforeAll
        static void setup() throws Exception {
            when(trackerSvc.getUserTrackers(isA(String.class))).thenReturn(TrackersGetResponse.fromTrackerDTOs(List.of(
                    t1)));
        }

        @Test
        void valid() throws Exception {
            mockMvc.perform(get("/trackers").header("Authorization", "Bearer " + userJwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("trackers[0].id").value(t1.id()))
                    .andExpect(jsonPath("trackers[0].name").value(t1.name()));
        }

        @Test
        void unauthorized_no_jwt() throws Exception {
            mockMvc.perform(get("/trackers")).andExpect(status().isUnauthorized());
        }

        @Test
        public void unauthorized_tracker_jwt() throws Exception {
            mockMvc.perform(get("/trackers").header("Authorization", "Bearer " + trackerJwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetTracker {

        @BeforeAll
        static void setup() throws Exception {
            when(trackerSvc.getTrackerById(isA(String.class))).thenReturn(new TrackerGetResponse(t1));
        }

        @Test
        void valid() throws Exception {
            mockMvc.perform(get("/trackers/%s".formatted(t1.id())).header("Authorization", "Bearer " + userJwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("tracker.id").value(t1.id()))
                    .andExpect(jsonPath("tracker.userId").value(t1.userId()))
                    .andExpect(jsonPath("tracker.name").value(t1.name()));
        }

        @Test
        void unauthorized_no_jwt() throws Exception {
            mockMvc.perform(get("/trackers/%s".formatted(t1.id()))).andExpect(status().isUnauthorized());
        }

        @Test
        public void unauthorized_tracker_jwt() throws Exception {
            mockMvc.perform(get("/trackers/%s".formatted(t1.id())).header("Authorization", "Bearer " + trackerJwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class RegisterTrackerData {
        private static TrackerDataRegisterRequest req;
        private static ObjectMapper om;


        @BeforeAll
        static void setup() {
            doNothing().when(trackerSvc).registerTrackerData(isA(TrackerDataDTO.class));
            req = new TrackerDataRegisterRequest(Map.of("param1", "10"), Instant.now());
            om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
        }

        @AfterAll
        static void tearDown() {
            req = null;
            om = null;
        }

        @Test
        void valid() throws Exception {
            mockMvc.perform(post("/trackers/data").header("Authorization", "Bearer " + trackerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(req))).andExpect(status().isOk());
        }

        @Test
        void invalid_data() throws Exception {
            var req = new TrackerDataRegisterRequest(null, Instant.now());

            mockMvc.perform(post("/trackers/data").header("Authorization", "Bearer " + trackerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(req))).andExpect(status().isBadRequest());
        }

        @Test
        void invalid_timestamp() throws Exception {
            var req = new TrackerDataRegisterRequest(Map.of("param1", "10"), null);

            mockMvc.perform(post("/trackers/data").header("Authorization", "Bearer " + trackerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(req))).andExpect(status().isBadRequest());
        }

        @Test
        void unauthorized_no_jwt() throws Exception {
            mockMvc.perform(post("/trackers/data").contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(req))).andExpect(status().isUnauthorized());
        }

        @Test
        public void unauthorized_user_jwt() throws Exception {
            mockMvc.perform(post("/trackers/data").header("Authorization", "Bearer " + userJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(req))).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetTrackerData {

        @BeforeAll
        static void setup() throws Exception {
            when(trackerSvc.getTrackerData(t1.id(),
                    null,
                    null,
                    null,
                    null)).thenReturn(TrackerDataGetResponse.fromTrackerDataDTOs(List.of(td1, td2)));
        }

        @Test
        public void valid() throws Exception {
            mockMvc.perform(get("/trackers/%s/data".formatted(t1.id())).header("Authorization", "Bearer " + userJwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("trackerData[0].data.param1").value(td1.data().get("param1")))
                    .andExpect(jsonPath("trackerData[0].timestamp").value(td1.timestamp().toString()))
                    .andExpect(jsonPath("trackerData[1].data.param1").value(td2.data().get("param1")))
                    .andExpect(jsonPath("trackerData[1].timestamp").value(td2.timestamp().toString()));
        }

        @Test
        public void unauthorized_no_jwt() throws Exception {
            mockMvc.perform(get("/trackers/%s/data".formatted(t1.id()))).andExpect(status().isUnauthorized());
        }

        @Test
        public void unauthorized_tracker_jwt() throws Exception {
            mockMvc.perform(get("/trackers/%s/data".formatted(t1.id())).header("Authorization", "Bearer " + trackerJwt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetLatestData {

        @BeforeAll
        static void setup() throws Exception {
            when(trackerSvc.getLatestTrackersData(t1.userId())).thenReturn(LatestTrackerDataGetResponse.fromTrackerDataDTOs(
                    List.of(td2)));
        }

        @Test
        public void valid() throws Exception {
            mockMvc.perform(get("/trackers/data/latest").header("Authorization", "Bearer " + userJwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("latestTrackerData[0].data.param1").value(td2.data().get("param1")))
                    .andExpect(jsonPath("latestTrackerData[0].timestamp").value(td2.timestamp().toString()));
        }

        @Test
        public void unauthorized_no_jwt() throws Exception {
            mockMvc.perform(get("/trackers/data/latest")).andExpect(status().isUnauthorized());
        }

        @Test
        public void unauthorized_tracker_jwt() throws Exception {
            mockMvc.perform(get("/trackers/data/latest").header("Authorization", "Bearer " + trackerJwt))
                    .andExpect(status().isUnauthorized());
        }
    }
}
