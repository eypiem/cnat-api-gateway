package dev.apma.cnat.apigateway.response;


import dev.apma.cnat.apigateway.dto.TrackerDTO;

public record TrackerRegisterResponse(TrackerDTO tracker, String accessToken) {
}
