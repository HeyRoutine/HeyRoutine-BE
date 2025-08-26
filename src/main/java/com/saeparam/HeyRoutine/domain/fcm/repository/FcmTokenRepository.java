package com.saeparam.HeyRoutine.domain.fcm.repository;

import com.saeparam.HeyRoutine.domain.fcm.entity.FcmToken;
import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
}