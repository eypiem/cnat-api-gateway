package dev.apma.cnat.apigateway.request;


import dev.apma.cnat.apigateway.dto.Tracker;

import java.time.Instant;
import java.util.Optional;

public record GetTrackerDataRequest(Tracker tracker, Optional<Instant> from, Optional<Instant> to) {
}
