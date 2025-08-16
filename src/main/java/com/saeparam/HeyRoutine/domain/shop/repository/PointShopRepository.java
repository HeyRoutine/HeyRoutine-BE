package com.saeparam.HeyRoutine.domain.shop.repository;

import com.saeparam.HeyRoutine.domain.shop.entity.PointShop;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

public interface PointShopRepository extends JpaRepository<PointShop, Long> {

}