package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotBlank;

/**
 * This request class defines the body for a <i>user delete</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record UserDeleteRequest(@NotBlank String email, @NotBlank String password) {
}
