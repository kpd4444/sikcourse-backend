package com.sikcourse.backend.domain.meal.repository;

import com.sikcourse.backend.domain.meal.entity.Menu;
import com.sikcourse.backend.domain.meal.entity.MenuType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByPlaceIdOrderByNameAsc(Long placeId);

    List<Menu> findAllByPlaceIdInOrderByNameAsc(List<Long> placeIds);

    List<Menu> findAllByPlaceIdInAndMenuTypeOrderByNameAsc(List<Long> placeIds, MenuType menuType);

    boolean existsByPlaceIdInAndMenuType(List<Long> placeIds, MenuType menuType);
}
