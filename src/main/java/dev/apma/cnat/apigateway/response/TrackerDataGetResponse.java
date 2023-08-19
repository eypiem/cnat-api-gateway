package dev.apma.cnat.apigateway.response;


import dev.apma.cnat.apigateway.dto.TrackerDataDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record TrackerDataGetResponse(Tracker tracker, List<TrackerData> trackerData) {

    record Tracker(String id, String name) {
    }

    record TrackerData(Map<String, Object> data, Instant timestamp) {
    }

    public static TrackerDataGetResponse fromTrackerDataDTOs(List<TrackerDataDTO> trackerDataDTO) {
        return new TrackerDataGetResponse(new Tracker(trackerDataDTO.get(0).tracker().id(),
                trackerDataDTO.get(0).tracker().name()),
                trackerDataDTO.stream().map(e -> new TrackerData(e.data(), e.timestamp())).toList());
    }
}
