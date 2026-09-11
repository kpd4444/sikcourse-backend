package com.sikcourse.backend.domain.place.repository;

import com.sikcourse.backend.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByContentId(String contentId);

    List<Place> findAllByOrderByTitleAsc();

    List<Place> findAllByAreaCodeOrderByTitleAsc(String areaCode);

    List<Place> findAllByAreaCodeAndSigunguCodeOrderByTitleAsc(String areaCode, String sigunguCode);
}
