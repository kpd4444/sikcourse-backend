package com.sikcourse.backend.domain.place.entity;

import com.sikcourse.backend.global.entity.BaseTimeEntity;
import com.sikcourse.backend.infra.tourapi.dto.TourRestaurantItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "places",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_places_content_id", columnNames = "content_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false, length = 30)
    private String contentId;

    @Column(name = "content_type_id", nullable = false, length = 10)
    private String contentTypeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String addr1;

    @Column(length = 300)
    private String addr2;

    @Column(name = "area_code", length = 20)
    private String areaCode;

    @Column(name = "sigungu_code", length = 20)
    private String sigunguCode;

    @Column(precision = 13, scale = 10)
    private BigDecimal mapX;

    @Column(precision = 13, scale = 10)
    private BigDecimal mapY;

    @Column(length = 100)
    private String tel;

    @Column(name = "first_image", length = 1000)
    private String firstImage;

    @Column(name = "first_image2", length = 1000)
    private String firstImage2;

    @Column(length = 20)
    private String cat1;

    @Column(length = 20)
    private String cat2;

    @Column(length = 20)
    private String cat3;

    @Builder
    private Place(
            String contentId,
            String contentTypeId,
            String title,
            String addr1,
            String addr2,
            String areaCode,
            String sigunguCode,
            BigDecimal mapX,
            BigDecimal mapY,
            String tel,
            String firstImage,
            String firstImage2,
            String cat1,
            String cat2,
            String cat3
    ) {
        this.contentId = contentId;
        this.contentTypeId = contentTypeId;
        this.title = title;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
        this.mapX = mapX;
        this.mapY = mapY;
        this.tel = tel;
        this.firstImage = firstImage;
        this.firstImage2 = firstImage2;
        this.cat1 = cat1;
        this.cat2 = cat2;
        this.cat3 = cat3;
    }

    public static Place from(TourRestaurantItem item) {
        return Place.builder()
                .contentId(item.contentid())
                .contentTypeId(item.contenttypeid())
                .title(item.title())
                .addr1(item.addr1())
                .addr2(item.addr2())
                .areaCode(item.areacode())
                .sigunguCode(item.sigungucode())
                .mapX(parseDecimal(item.mapx()))
                .mapY(parseDecimal(item.mapy()))
                .tel(item.tel())
                .firstImage(item.firstimage())
                .firstImage2(item.firstimage2())
                .cat1(item.cat1())
                .cat2(item.cat2())
                .cat3(item.cat3())
                .build();
    }

    public void updateFrom(TourRestaurantItem item) {
        this.contentTypeId = item.contenttypeid();
        this.title = item.title();
        this.addr1 = item.addr1();
        this.addr2 = item.addr2();
        this.areaCode = item.areacode();
        this.sigunguCode = item.sigungucode();
        this.mapX = parseDecimal(item.mapx());
        this.mapY = parseDecimal(item.mapy());
        this.tel = item.tel();
        this.firstImage = item.firstimage();
        this.firstImage2 = item.firstimage2();
        this.cat1 = item.cat1();
        this.cat2 = item.cat2();
        this.cat3 = item.cat3();
    }

    private static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }
}
