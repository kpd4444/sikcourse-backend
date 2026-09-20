package com.sikcourse.backend.infra.aimenu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sikcourse.backend.global.config.AiMenuMasterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuNutritionClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AiMenuMasterProperties properties;

    public Optional<MenuNutritionMatchResponse> match(String menuName) {
        try {
            String requestBody = OBJECT_MAPPER.writeValueAsString(
                    Map.of("menu_name", menuName)
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.baseUrl() + "/match"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> httpResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
                log.warn("AI menu nutrition match failed for menuName={}: status={}, body={}",
                        menuName,
                        httpResponse.statusCode(),
                        httpResponse.body());
                log.warn("AI menu nutrition request body: {}", requestBody);
                return Optional.empty();
            }

            MenuNutritionMatchResponse response = OBJECT_MAPPER.readValue(
                    httpResponse.body(),
                    MenuNutritionMatchResponse.class
            );

            return Optional.ofNullable(response);
        } catch (Exception exception) {
            log.warn("AI menu nutrition match failed for menuName={}: {}: {}",
                    menuName,
                    exception.getClass().getSimpleName(),
                    exception.getMessage());
            return Optional.empty();
        }
    }
}
