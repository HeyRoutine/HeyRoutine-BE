package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface RoutineRepository extends JpaRepository<Routine, Long> {
    /**
     * 특정 MyRoutineList ID에 속한 모든 Routine 목록을 조회합니다.
     */

    @Query("SELECT r FROM Routine r JOIN MyRoutineMiddle mrm ON r.id = mrm.routine.id " +
            "WHERE mrm.routineList.id = :routineListId")
    List<Routine> findAllByRoutineListId(Long routineListId);

}