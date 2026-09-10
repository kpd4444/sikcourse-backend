package com.sikcourse.backend.infra.tourapi.dto;

import tools.jackson.databind.JsonNode;

public record TourApiEnvelope(
        Response response
) {

    public record Response(
            Header header,
            Body body
    ) {
    }

    public record Header(
            String resultCode,
            String resultMsg
    ) {
    }

    public record Body(
            Integer numOfRows,
            Integer pageNo,
            Integer totalCount,
            JsonNode items
    ) {
    }
}
