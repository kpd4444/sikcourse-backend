package com.sikcourse.backend.domain.trip.entity;

import com.sikcourse.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "trips")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trip extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "area_code", nullable = false, length = 20)
    private String areaCode;

    @Column(name = "sigungu_code", length = 20)
    private String sigunguCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "base_map_x", precision = 13, scale = 10)
    private BigDecimal baseMapX;

    @Column(name = "base_map_y", precision = 13, scale = 10)
    private BigDecimal baseMapY;

    @Builder
    private Trip(
            Long userId,
            String title,
            String areaCode,
            String sigunguCode,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal baseMapX,
            BigDecimal baseMapY
    ) {
        this.userId = userId;
        this.title = title;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.baseMapX = baseMapX;
        this.baseMapY = baseMapY;
    }

    public void update(
            String title,
            String areaCode,
            String sigunguCode,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal baseMapX,
            BigDecimal baseMapY
    ) {
        if (title != null) {
            this.title = title;
        }
        if (areaCode != null) {
            this.areaCode = areaCode;
        }
        if (sigunguCode != null) {
            this.sigunguCode = sigunguCode;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
        if (baseMapX != null) {
            this.baseMapX = baseMapX;
        }
        if (baseMapY != null) {
            this.baseMapY = baseMapY;
        }
    }
}
