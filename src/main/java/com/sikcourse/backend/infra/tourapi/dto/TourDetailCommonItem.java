package com.sikcourse.backend.infra.tourapi.dto;

public record TourDetailCommonItem(
        String contentid,
        String contenttypeid,
        String title,
        String tel,
        String telname,
        String homepage,
        String firstimage,
        String firstimage2,
        String cpyrhtDivCd,
        String areacode,
        String sigungucode,
        String cat1,
        String cat2,
        String cat3,
        String addr1,
        String addr2,
        String zipcode,
        String mapx,
        String mapy,
        String mlevel,
        String overview
) {
}
