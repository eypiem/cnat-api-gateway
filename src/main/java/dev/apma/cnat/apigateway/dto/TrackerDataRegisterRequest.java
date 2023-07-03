package dev.apma.cnat.apigateway.dto;


import java.time.Instant;
import java.util.Map;

public record TrackerDataRegisterRequest(String trackerId, Map<String, Object> data, Instant timestamp) {
}
