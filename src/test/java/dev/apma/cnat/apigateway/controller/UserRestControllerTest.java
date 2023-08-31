package dev.apma.cnat.apigateway.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserDeleteRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import dev.apma.cnat.apigateway.service.JwtService;
import dev.apma.cnat.apigateway.service.TrackerService;
import dev.apma.cnat.apigateway.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtHelper;

    @MockBean
    private static UserService userSvc;

    @MockBean
    private static TrackerService trackerSvc;

    private static String userJwt;

    @BeforeAll
    void setup() {
        userJwt = jwtHelper.createJwtForClaims("1@test.com",
                Map.of(JwtService.ROLE_ATTRIBUTE, JwtService.Role.USER.toString()));
    }

    @AfterAll
    void tearDown() {
        userJwt = null;
    }

    @Nested
    class Register {

        @BeforeAll
        static void setup() throws Exception {
            doNothing().when(userSvc).register(isA(UserRegisterRequest.class));
        }

        @Test
        void register_valid() throws Exception {
            var urr = new UserRegisterRequest("1@test.com", "password1", "fn1", "ln1");

            mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(urr))).andExpect(status().isOk());
        }

        @Test
        void register_invalid_empty_body() throws Exception {
            mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void register_invalid_email(String email) throws Exception {
            var urr = new UserRegisterRequest(email, "password1", "fn1", "ln1");

            mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(urr))).andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void register_invalid_password(String password) throws Exception {
            var urr = new UserRegisterRequest("a@a.com", password, "fn1", "ln1");

            mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(urr))).andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void register_invalid_firstName(String firstName) throws Exception {
            var urr = new UserRegisterRequest("a@a.com", "password1", firstName, "ln1");

            mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(urr))).andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void register_invalid_lastName(String lastName) throws Exception {
            var urr = new UserRegisterRequest("a@a.com", "password1", "fn1", lastName);

            mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(urr))).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Auth {
        static UserAuthResponse uares = new UserAuthResponse("1@test.com", userJwt);

        @BeforeAll
        static void setup() throws Exception {
            when(userSvc.auth(isA(UserAuthRequest.class))).thenReturn(uares);
        }

        @Test
        void auth_valid() throws Exception {
            var uareq = new UserAuthRequest("1@test.com", "password1");

            mockMvc.perform(post("/users/auth").contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(uareq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("email").value(uares.email()))
                    .andExpect(jsonPath("accessToken").value(uares.accessToken()));
        }

        @Test
        void auth_invalid_empty_body() throws Exception {
            mockMvc.perform(post("/users/auth")).andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void auth_invalid_email(String email) throws Exception {
            var uareq = new UserAuthRequest(email, "password1");

            mockMvc.perform(post("/users/auth").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(uareq))).andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void auth_invalid_password(String password) throws Exception {
            var uareq = new UserAuthRequest("1@test.com", password);

            mockMvc.perform(post("/users/auth").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(uareq))).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Delete {

        @BeforeAll
        static void setup() throws Exception {
            doNothing().when(trackerSvc).deleteAllUserTrackers(isA(String.class));
        }

        @Test
        void delete_valid() throws Exception {
            var udr = new UserDeleteRequest("1@test.com", "password1");

            mockMvc.perform(delete("/users").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(udr))).andExpect(status().isOk());
        }

        @Test
        void delete_empty_body() throws Exception {
            mockMvc.perform(delete("/users")).andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void delete_invalid_email(String email) throws Exception {
            var udr = new UserDeleteRequest(email, "password1");

            mockMvc.perform(delete("/users").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(udr))).andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void delete_invalid_password(String password) throws Exception {
            var udr = new UserDeleteRequest("1@test.com", password);

            mockMvc.perform(delete("/users").contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(udr))).andExpect(status().isBadRequest());
        }
    }
}
