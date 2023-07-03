package dev.apma.cnat.apigateway.dto;


import java.time.Instant;
import java.util.Map;

public record TrackerData(Tracker tracker, Map<String, Object> data, Instant timestamp) {
}
