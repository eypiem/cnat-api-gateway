package dev.apma.cnat.apigateway.controller;


import dev.apma.cnat.apigateway.jwt.JwtHelper;
import dev.apma.cnat.apigateway.request.UserRegisterRequest;
import dev.apma.cnat.apigateway.response.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
public class UserRestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);

    @Value("${app.cnat.user-service}")
    private String userServiceUri;

    @PostMapping("/register")
    public GenericResponse register(@RequestBody UserRegisterRequest user) {
        LOGGER.info("/user/register: {}", user);

        String uri = userServiceUri + "/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserRegisterRequest> request = new HttpEntity<>(user, headers);

        try {
            return new RestTemplate().postForObject(uri, request, GenericResponse.class);
        } catch (HttpStatusCodeException e) {
            GenericResponse gr = e.getResponseBodyAs(GenericResponse.class);
            throw new ResponseStatusException(e.getStatusCode(), gr != null ? gr.message() : null);
        } catch (RestClientException e) {
            LOGGER.error("Error in communicating with cnat-tracker-service: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error in communicating with cnat-tracker-service");
        }
    }

    @DeleteMapping("/delete")
    public GenericResponse delete(Authentication auth) {
        LOGGER.info("/delete");

        return JwtHelper.onRoleMatchOrElseThrow(auth, JwtHelper.Role.USER, (subject) -> {
            /// TODO: Implement
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        });
    }
}
