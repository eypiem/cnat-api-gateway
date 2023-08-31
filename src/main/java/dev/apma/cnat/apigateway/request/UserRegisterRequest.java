package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotBlank;

/**
 * This request class defines the body for a <i>user register</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record UserRegisterRequest(@NotBlank String email,
                                  @NotBlank String password,
                                  @NotBlank String firstName,
                                  @NotBlank String lastName) {
}
