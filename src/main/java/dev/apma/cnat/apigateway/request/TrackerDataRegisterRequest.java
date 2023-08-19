package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record TrackerDataRegisterRequest(@NotNull Map<String, Object> data, @NotNull Instant timestamp) {
}
