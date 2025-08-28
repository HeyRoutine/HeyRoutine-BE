package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.UserWeeklyPlanWithRoutine;
import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserWeeklyPlanWithRoutineRepository extends JpaRepository<UserWeeklyPlanWithRoutine, Long> {
}