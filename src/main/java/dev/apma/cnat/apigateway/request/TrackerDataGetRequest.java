package dev.apma.cnat.apigateway.request;


import dev.apma.cnat.apigateway.dto.Tracker;

import java.time.Instant;
import java.util.Optional;

public record TrackerDataGetRequest(Tracker tracker, Optional<Instant> from, Optional<Instant> to) {
}
