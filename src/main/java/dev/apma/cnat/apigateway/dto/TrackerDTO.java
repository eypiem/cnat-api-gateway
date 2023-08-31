package dev.apma.cnat.apigateway.dto;


/**
 * This DTO class represents the tracker format returned from CNAT Tracker Service.
 *
 * @author Amir Parsa Mahdian
 */
public record TrackerDTO(String id, String userId, String name) {
}
