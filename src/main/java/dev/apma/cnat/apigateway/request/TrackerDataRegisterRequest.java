package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * This request class defines the body for a <i>tracker data register</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record TrackerDataRegisterRequest(@NotNull Map<String, Object> data, @NotNull Instant timestamp) {
}
