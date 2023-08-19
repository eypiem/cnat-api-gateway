package dev.apma.cnat.apigateway.response;


import dev.apma.cnat.apigateway.dto.TrackerDataDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LatestTrackerDataGetResponse(List<TrackerData> latestTrackerData) {

    record Tracker(String id, String name) {
    }

    record TrackerData(Tracker tracker, Map<String, Object> data, Instant timestamp) {
    }

    public static LatestTrackerDataGetResponse fromTrackerDataDTOs(List<TrackerDataDTO> trackerDataDTO) {
        return new LatestTrackerDataGetResponse(trackerDataDTO.stream()
                .map(e -> new TrackerData(new Tracker(e.tracker().id(), e.tracker().name()), e.data(), e.timestamp()))
                .toList());
    }
}
