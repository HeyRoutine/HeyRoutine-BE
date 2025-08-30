package com.saeparam.HeyRoutine.domain.fcm.repository;

import com.saeparam.HeyRoutine.domain.fcm.entity.FcmToken;
import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    void deleteAllByUser(User user);


    Optional<FcmToken> findByUser(User user);
}