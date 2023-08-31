package dev.apma.cnat.apigateway.response;


import dev.apma.cnat.apigateway.dto.TrackerDTO;

/**
 * This response class defines the body for a <i>tracker get</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record TrackerGetResponse(TrackerDTO tracker) {
}
