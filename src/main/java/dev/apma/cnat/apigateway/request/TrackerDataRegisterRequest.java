package dev.apma.cnat.apigateway.request;


import java.time.Instant;
import java.util.Map;

public record TrackerDataRegisterRequest(String trackerId, Map<String, Object> data, Instant timestamp) {
}
