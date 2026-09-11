package com.sikcourse.backend.infra.tourapi;

import com.sikcourse.backend.global.config.TourApiProperties;
import com.sikcourse.backend.infra.tourapi.dto.TourApiEnvelope;
import com.sikcourse.backend.infra.tourapi.dto.TourApiResponse;
import com.sikcourse.backend.infra.tourapi.dto.TourDetailCommonItem;
import com.sikcourse.backend.infra.tourapi.dto.TourDetailIntroItem;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    private static final String RESTAURANT_CONTENT_TYPE_ID = "39";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final TourApiProperties properties;

    public TourApiResponse<TourRestaurantItem> areaBasedList2(String areaCode, Integer pageNo, Integer numOfRows) {
        return areaBasedList2(areaCode, null, pageNo, numOfRows);
    }

    public TourApiResponse<TourRestaurantItem> areaBasedList2(
            String areaCode,
            String sigunguCode,
            Integer pageNo,
            Integer numOfRows
    ) {
        Map<String, String> params = defaultParams(pageNo, numOfRows);
        params.put("contentTypeId", RESTAURANT_CONTENT_TYPE_ID);
        params.put("areaCode", areaCode);
        if (sigunguCode != null && !sigunguCode.isBlank()) {
            params.put("sigunguCode", sigunguCode);
        }
        params.put("arrange", "A");

        return get("/areaBasedList2", params, TourRestaurantItem.class);
    }

    public TourApiResponse<TourRestaurantItem> locationBasedList2(
            String mapX,
            String mapY,
            Integer radius,
            Integer pageNo,
            Integer numOfRows
    ) {
        Map<String, String> params = defaultParams(pageNo, numOfRows);
        params.put("contentTypeId", RESTAURANT_CONTENT_TYPE_ID);
        params.put("mapX", mapX);
        params.put("mapY", mapY);
        params.put("radius", String.valueOf(radius));
        params.put("arrange", "A");

        return get("/locationBasedList2", params, TourRestaurantItem.class);
    }

    public TourApiResponse<TourDetailCommonItem> detailCommon2(String contentId) {
        Map<String, String> params = defaultParams(1, 10);
        params.put("contentId", contentId);
        params.put("contentTypeId", RESTAURANT_CONTENT_TYPE_ID);
        params.put("defaultYN", "Y");
        params.put("firstImageYN", "Y");
        params.put("addrinfoYN", "Y");
        params.put("mapinfoYN", "Y");
        params.put("overviewYN", "Y");

        return get("/detailCommon2", params, TourDetailCommonItem.class);
    }

    public TourApiResponse<TourDetailIntroItem> detailIntro2(String contentId) {
        Map<String, String> params = defaultParams(1, 10);
        params.put("contentId", contentId);
        params.put("contentTypeId", RESTAURANT_CONTENT_TYPE_ID);

        return get("/detailIntro2", params, TourDetailIntroItem.class);
    }

    private <T> TourApiResponse<T> get(String path, Map<String, String> params, Class<T> itemType) {
        URI uri = tourApiUri(path, params);
        String responseBody = restClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        return parseResponse(responseBody, itemType);
    }

    private Map<String, String> defaultParams(Integer pageNo, Integer numOfRows) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("serviceKey", properties.serviceKey());
        params.put("MobileOS", properties.mobileOs());
        params.put("MobileApp", properties.mobileApp());
        params.put("_type", "json");
        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(numOfRows));
        return params;
    }

    private URI tourApiUri(String path, Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        params.forEach((key, value) -> {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(urlEncode(key))
                    .append('=')
                    .append(urlEncode(value));
        });

        return URI.create(properties.baseUrl() + path + "?" + query);
    }

    private String urlEncode(String value) {
        if (value == null) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private <T> TourApiResponse<T> parseResponse(String responseBody, Class<T> itemType) {
        try {
            TourApiEnvelope envelope = objectMapper.readValue(responseBody, TourApiEnvelope.class);
            TourApiEnvelope.Response response = envelope.response();
            TourApiEnvelope.Body body = response.body();

            return new TourApiResponse<>(
                    new TourApiResponse.Header(response.header().resultCode(), response.header().resultMsg()),
                    new TourApiResponse.Body<>(
                            body.numOfRows(),
                            body.pageNo(),
                            body.totalCount(),
                            extractItems(body.items(), itemType)
                    )
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException("TourAPI 응답 파싱에 실패했습니다.", exception);
        }
    }

    private <T> List<T> extractItems(JsonNode itemsNode, Class<T> itemType) throws JacksonException {
        if (itemsNode == null || itemsNode.isNull() || itemsNode.isMissingNode() || isBlankText(itemsNode)) {
            return List.of();
        }

        JsonNode itemNode = itemsNode.get("item");
        if (itemNode == null || itemNode.isNull() || itemNode.isMissingNode() || isBlankText(itemNode)) {
            return List.of();
        }

        if (itemNode.isArray()) {
            List<T> items = new ArrayList<>();
            for (JsonNode node : itemNode) {
                items.add(objectMapper.treeToValue(node, itemType));
            }
            return items;
        }

        return List.of(objectMapper.treeToValue(itemNode, itemType));
    }

    private boolean isBlankText(JsonNode node) {
        return node.isTextual() && node.asText().isBlank();
    }
}
