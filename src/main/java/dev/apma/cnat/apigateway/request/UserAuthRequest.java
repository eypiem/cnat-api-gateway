package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotBlank;

/**
 * This request class defines the body for a <i>user authentication</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record UserAuthRequest(@NotBlank String email, @NotBlank String password) {
}
