package dev.apma.cnat.apigateway.dto;


import java.time.Instant;
import java.util.Map;

/**
 * This DTO class represents the tracker data format returned from CNAT Tracker Service.
 *
 * @author Amir Parsa Mahdian
 */
public record TrackerDataDTO(TrackerDTO tracker, Map<String, Object> data, Instant timestamp) {
}
