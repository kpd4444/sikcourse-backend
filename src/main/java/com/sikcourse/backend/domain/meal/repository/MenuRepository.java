package com.sikcourse.backend.domain.meal.repository;

import com.sikcourse.backend.domain.meal.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByPlaceIdOrderByNameAsc(Long placeId);
}
