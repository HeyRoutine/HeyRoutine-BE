package com.saeparam.HeyRoutine.domain.test;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

}