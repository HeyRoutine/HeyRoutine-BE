package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.Routine;
import com.saeparam.HeyRoutine.domain.routine.entity.RoutineRecord;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface RoutineRecordRepository extends JpaRepository<RoutineRecord, Long> {


    @Query("SELECT rr FROM RoutineRecord rr " +
            "WHERE rr.user = :user " +
            "AND rr.createdDate >= :startOfDay AND rr.createdDate <= :endOfDay " + // <-- date -> createdDate로 수정
            "AND rr.routine IN :routines")
    List<RoutineRecord> findRecordsByDateAndRoutines(User user, LocalDateTime startOfDay, LocalDateTime endOfDay, List<Routine> routines
    );


    @Query("SELECT rr FROM RoutineRecord rr " +
            "WHERE rr.user = :user " +
            "AND rr.routine = :routine " +
            "AND rr.createdDate >= :startOfDay AND rr.createdDate <= :endOfDay")
    Optional<RoutineRecord> findRecordByDateAndRoutine(User user,
            Routine routine,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );


}

