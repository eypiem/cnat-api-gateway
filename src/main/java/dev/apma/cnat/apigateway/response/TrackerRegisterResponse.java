package dev.apma.cnat.apigateway.response;


import dev.apma.cnat.apigateway.dto.Tracker;

public record TrackerRegisterResponse(Tracker tracker, String accessToken) {
}
