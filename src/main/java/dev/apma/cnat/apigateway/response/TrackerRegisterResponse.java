package dev.apma.cnat.apigateway.response;


import dev.apma.cnat.apigateway.dto.TrackerDTO;

/**
 * This response class defines the body for a <i>tracker register</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record TrackerRegisterResponse(TrackerDTO tracker, String accessToken) {
}
