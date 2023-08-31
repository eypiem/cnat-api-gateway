package dev.apma.cnat.apigateway.response;


/**
 * This response class defines the body for a <i>user authentication</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record UserAuthResponse(String email, String accessToken) {
}
