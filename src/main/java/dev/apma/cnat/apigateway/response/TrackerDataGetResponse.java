package dev.apma.cnat.apigateway.response;


import dev.apma.cnat.apigateway.dto.TrackerDataDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * This response class defines the body for a <i>tracker data get</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record TrackerDataGetResponse(List<TrackerData> trackerData) {

    record TrackerData(Map<String, Object> data, Instant timestamp) {
    }

    public static TrackerDataGetResponse fromTrackerDataDTOs(List<TrackerDataDTO> trackerDataDTO) {
        return new TrackerDataGetResponse(trackerDataDTO.stream()
                .map(e -> new TrackerData(e.data(), e.timestamp()))
                .toList());
    }
}
