package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineListRecord;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MyRoutineListRecordRepository extends JpaRepository<MyRoutineListRecord, Long> {

    @Query("SELECT r FROM MyRoutineListRecord r " +
            "WHERE r.user = :user " +
            "AND r.myRoutineList = :routineList " +
            "AND r.createdDate BETWEEN :startOfDay AND :endOfDay")
    Optional<MyRoutineListRecord> findByUserAndMyRoutineListAndCreatedDateBetween(User user, MyRoutineList routineList, LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<MyRoutineListRecord> findByUserAndCreatedDateBetween(User user, LocalDateTime start, LocalDateTime end);
}