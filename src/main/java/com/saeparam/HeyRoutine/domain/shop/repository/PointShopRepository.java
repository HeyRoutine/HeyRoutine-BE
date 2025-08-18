package com.saeparam.HeyRoutine.domain.shop.repository;

import com.saeparam.HeyRoutine.domain.shop.entity.PointShop;
import com.saeparam.HeyRoutine.domain.shop.enums.PointShopCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface PointShopRepository extends JpaRepository<PointShop, Long> {

    Page<PointShop> findByCategory(PointShopCategory category, Pageable pageable);
}