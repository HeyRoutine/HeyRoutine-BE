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

    /**
     * 특정 루틴에 대한 모든 수행 기록을 삭제합니다.
     *
     * @param routine 삭제 대상 루틴
     */
    void deleteAllByRoutine(Routine routine);

    @Query("SELECT count(rr) FROM RoutineRecord rr " +
            "WHERE rr.user = :user " +
            "AND rr.createdDate >= :startOfDay AND rr.createdDate <= :endOfDay " +
            "AND rr.routine IN :routines AND rr.doneCheck = true")
    long countCompletedRoutinesInList(User user, LocalDateTime startOfDay, LocalDateTime endOfDay, List<Routine> routines);

    /**
     * 특정 사용자의 모든 루틴 수행 기록을 삭제합니다.
     *
     * @param user 삭제 대상 사용자
     */
    void deleteAllByUser(User user);
}

