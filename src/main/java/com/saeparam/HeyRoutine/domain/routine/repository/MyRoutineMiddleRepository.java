package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineMiddle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyRoutineMiddleRepository extends JpaRepository<MyRoutineMiddle, Long> {

}