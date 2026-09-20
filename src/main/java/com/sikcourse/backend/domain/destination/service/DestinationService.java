package com.sikcourse.backend.domain.destination.service;

import com.sikcourse.backend.domain.destination.dto.DestinationResolveResponse;
import com.sikcourse.backend.infra.tourapi.TourApiClient;
import com.sikcourse.backend.infra.tourapi.dto.TourAreaCodeItem;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final TourApiClient tourApiClient;

    public DestinationResolveResponse resolve(String query) {
        String normalizedQuery = normalize(query);
        List<TourAreaCodeItem> areas = tourApiClient.areaCode2().body().items();
        TourAreaCodeItem area = findBestMatch(areas, normalizedQuery);
        if (area == null) {
            return resolveBySigungu(query, normalizedQuery, areas);
        }

        TourAreaCodeItem sigungu = findBestMatch(
                tourApiClient.areaCode2(area.code()).body().items(),
                normalizedQuery
        );
        if (sigungu == null) {
            DestinationResolveResponse resolvedByPlace = resolveByPlaceKeyword(query, area);
            if (resolvedByPlace.sigunguCode() != null) {
                return resolvedByPlace;
            }
        }

        return new DestinationResolveResponse(
                query,
                area.code(),
                area.name(),
                sigungu == null ? null : sigungu.code(),
                sigungu == null ? null : sigungu.name()
        );
    }

    private DestinationResolveResponse resolveBySigungu(
            String query,
            String normalizedQuery,
            List<TourAreaCodeItem> areas
    ) {
        for (TourAreaCodeItem candidateArea : areas) {
            TourAreaCodeItem sigungu = findBestMatch(
                    tourApiClient.areaCode2(candidateArea.code()).body().items(),
                    normalizedQuery
            );
            if (sigungu != null) {
                return new DestinationResolveResponse(
                        query,
                        candidateArea.code(),
                        candidateArea.name(),
                        sigungu.code(),
                        sigungu.name()
                );
            }
        }

        return new DestinationResolveResponse(query, null, null, null, null);
    }

    private DestinationResolveResponse resolveByPlaceKeyword(String query, TourAreaCodeItem area) {
        String keyword = placeKeyword(query, area);
        if (keyword.isBlank()) {
            return new DestinationResolveResponse(query, area.code(), area.name(), null, null);
        }

        List<TourRestaurantItem> places = tourApiClient.searchKeyword2(keyword, 1, 10).body().items();
        return places.stream()
                .filter(place -> area.code().equals(place.areacode()))
                .filter(place -> place.sigungucode() != null && !place.sigungucode().isBlank())
                .findFirst()
                .map(place -> {
                    TourAreaCodeItem sigungu = findByCode(
                            tourApiClient.areaCode2(area.code()).body().items(),
                            place.sigungucode()
                    );
                    return new DestinationResolveResponse(
                            query,
                            area.code(),
                            area.name(),
                            place.sigungucode(),
                            sigungu == null ? null : sigungu.name()
                    );
                })
                .orElseGet(() -> new DestinationResolveResponse(query, area.code(), area.name(), null, null));
    }

    private String placeKeyword(String query, TourAreaCodeItem area) {
        return query.replace(area.name(), "")
                .replace(stripAdministrativeSuffix(area.name()), "")
                .trim();
    }

    private TourAreaCodeItem findByCode(List<TourAreaCodeItem> items, String code) {
        return items.stream()
                .filter(item -> item.code().equals(code))
                .findFirst()
                .orElse(null);
    }

    private TourAreaCodeItem findBestMatch(List<TourAreaCodeItem> items, String normalizedQuery) {
        return items.stream()
                .filter(item -> matches(normalizedQuery, item.name()))
                .findFirst()
                .orElse(null);
    }

    private boolean matches(String normalizedQuery, String name) {
        String normalizedName = normalize(name);
        String strippedName = stripAdministrativeSuffix(normalizedName);
        return normalizedQuery.contains(normalizedName)
                || (!strippedName.isBlank() && normalizedQuery.contains(strippedName));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "")
                .replace("특별자치도", "")
                .replace("특별자치시", "")
                .replace("특별시", "")
                .replace("광역시", "");
    }

    private String stripAdministrativeSuffix(String value) {
        return value.replaceAll("(도|시|군|구)$", "");
    }
}
