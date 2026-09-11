package com.sikcourse.backend.domain.trip.repository;

import com.sikcourse.backend.domain.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByUserIdOrderByStartDateDescIdDesc(Long userId);

    Optional<Trip> findByIdAndUserId(Long id, Long userId);
}
