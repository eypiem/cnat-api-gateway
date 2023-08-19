package dev.apma.cnat.apigateway.response;


import dev.apma.cnat.apigateway.dto.TrackerDTO;

import java.util.List;

public record TrackersGetResponse(List<Tracker> trackers) {
    record Tracker(String id, String name) {
    }

    public static TrackersGetResponse fromTrackerDTOs(List<TrackerDTO> trackerDTOs) {
        return new TrackersGetResponse(trackerDTOs.stream().map(e -> new Tracker(e.id(), e.name())).toList());
    }
}
