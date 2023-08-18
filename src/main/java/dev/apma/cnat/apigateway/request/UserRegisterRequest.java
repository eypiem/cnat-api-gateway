package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequest(@NotBlank String email,
                                  @NotBlank String password,
                                  @NotBlank String firstName,
                                  @NotBlank String lastName) {
}
