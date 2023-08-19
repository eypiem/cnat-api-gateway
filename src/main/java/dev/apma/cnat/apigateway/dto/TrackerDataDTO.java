package dev.apma.cnat.apigateway.dto;


import java.time.Instant;
import java.util.Map;

public record TrackerDataDTO(TrackerDTO tracker, Map<String, Object> data, Instant timestamp) {
}
