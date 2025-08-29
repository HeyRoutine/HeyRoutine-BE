package com.saeparam.HeyRoutine.domain.user.repository;

import com.saeparam.HeyRoutine.domain.user.entity.UserSurveyFlags;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSurveyFlagsRepository extends JpaRepository<UserSurveyFlags, Long> {


    Optional<UserSurveyFlags> findByUserId(String string);
}