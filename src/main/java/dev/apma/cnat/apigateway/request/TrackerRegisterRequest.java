package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotBlank;

public record TrackerRegisterRequest(@NotBlank String name) {
}
