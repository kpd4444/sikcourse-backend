package com.sikcourse.backend.domain.meal.repository;

import com.sikcourse.backend.domain.meal.entity.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    List<MealRecord> findAllByUserIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtDesc(
            Long userId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
