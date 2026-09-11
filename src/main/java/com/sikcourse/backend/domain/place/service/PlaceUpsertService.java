package com.sikcourse.backend.domain.place.service;

import com.sikcourse.backend.domain.place.entity.Place;
import com.sikcourse.backend.domain.place.entity.PlaceType;
import com.sikcourse.backend.domain.place.repository.PlaceRepository;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceUpsertService {

    private static final String CONTENT_ID_UNIQUE_CONSTRAINT = "uk_places_content_id";

    private final PlaceRepository placeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlaceUpsertResult upsert(TourRestaurantItem item) {
        return upsert(item, PlaceType.RESTAURANT);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlaceUpsertResult upsert(TourRestaurantItem item, PlaceType placeType) {
        Place place = placeRepository.findByContentId(item.contentid()).orElse(null);
        if (place == null) {
            try {
                placeRepository.saveAndFlush(Place.from(item, placeType));
                return PlaceUpsertResult.CREATED;
            } catch (DataIntegrityViolationException exception) {
                if (isContentIdUniqueConstraintViolation(exception)) {
                    throw new DuplicatePlaceContentIdException(item.contentid(), exception);
                }
                throw exception;
            }
        }

        place.updateFrom(item, placeType);
        return PlaceUpsertResult.UPDATED;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlaceUpsertResult updateExisting(TourRestaurantItem item) {
        return updateExisting(item, PlaceType.RESTAURANT);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlaceUpsertResult updateExisting(TourRestaurantItem item, PlaceType placeType) {
        Place place = placeRepository.findByContentId(item.contentid())
                .orElseThrow(() -> new DuplicatePlaceContentIdException(item.contentid()));
        place.updateFrom(item, placeType);
        return PlaceUpsertResult.UPDATED;
    }

    private boolean isContentIdUniqueConstraintViolation(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolationException) {
                return CONTENT_ID_UNIQUE_CONSTRAINT.equalsIgnoreCase(
                        constraintViolationException.getConstraintName()
                );
            }
            current = current.getCause();
        }
        return false;
    }
}
