package com.sikcourse.backend.infra.tourapi.dto;

import java.util.List;

public record TourApiResponse<T>(
        Header header,
        Body<T> body
) {

    public record Header(
            String resultCode,
            String resultMsg
    ) {
    }

    public record Body<T>(
            Integer numOfRows,
            Integer pageNo,
            Integer totalCount,
            List<T> items
    ) {
    }
}
