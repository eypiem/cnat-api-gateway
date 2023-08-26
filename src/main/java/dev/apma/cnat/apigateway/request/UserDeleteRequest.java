package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotBlank;

public record UserDeleteRequest(@NotBlank String email, @NotBlank String password) {
}
