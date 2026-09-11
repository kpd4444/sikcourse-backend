package com.sikcourse.backend.domain.health.repository;

import com.sikcourse.backend.domain.health.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {

    boolean existsByUserId(Long userId);

    Optional<HealthProfile> findByUserId(Long userId);
}
