package dev.apma.cnat.apigateway.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.apma.cnat.apigateway.request.UserAuthRequest;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.GenericResponse;
import dev.apma.cnat.apigateway.response.UserAuthResponse;
import dev.apma.cnat.apigateway.service.JwtHelper;
import dev.apma.cnat.apigateway.service.UserService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
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
    private JwtHelper jwtHelper;

    private String userJwt;

    @MockBean
    private UserService userSvc;

    @BeforeAll
    void setup() {
        userJwt = jwtHelper.createJwtForClaims("1@test.com",
                Map.of(JwtHelper.ROLE_ATTRIBUTE, JwtHelper.Role.USER.toString()));
    }

    @AfterAll
    void tearDown() {
        userJwt = null;
    }

    @Test
    public void register_valid() throws Exception {
        when(userSvc.register(isA(UserRegisterRequest.class))).thenReturn(new GenericResponse("OK"));
        var urr = new UserRegisterRequest("1@test.com", "password1", "fn1", "ln1");

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(urr))).andExpect(status().isOk());
    }

    @Test
    public void register_invalid_1() throws Exception {
        when(userSvc.register(isA(UserRegisterRequest.class))).thenReturn(new GenericResponse("OK"));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void register_invalid_2() throws Exception {
        when(userSvc.register(isA(UserRegisterRequest.class))).thenReturn(new GenericResponse("OK"));
        var urr = new UserRegisterRequest(null, "password1", "fn1", "ln1");

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(urr))).andExpect(status().isBadRequest());
    }

    @Test
    public void auth_valid() throws Exception {
        var uareq = new UserAuthRequest("1@test.com", "password1");
        var uares = new UserAuthResponse("1@test.com", userJwt);

        when(userSvc.auth(isA(UserAuthRequest.class))).thenReturn(uares);

        mockMvc.perform(post("/users/auth").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(uareq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(uares.email()))
                .andExpect(jsonPath("accessToken").value(uares.accessToken()));
    }

    @Test
    public void auth_invalid_1() throws Exception {
        var uares = new UserAuthResponse("1@test.com", userJwt);

        when(userSvc.auth(isA(UserAuthRequest.class))).thenReturn(uares);

        mockMvc.perform(post("/users/auth")).andExpect(status().isBadRequest());
    }

    @Test
    public void auth_invalid_2() throws Exception {
        var uareq = new UserAuthRequest("1@test.com", null);
        var uares = new UserAuthResponse("1@test.com", userJwt);

        when(userSvc.auth(isA(UserAuthRequest.class))).thenReturn(uares);

        mockMvc.perform(post("/users/auth").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(uareq))).andExpect(status().isBadRequest());
    }
}
