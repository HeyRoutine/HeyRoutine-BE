package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MyRoutineListRepository extends JpaRepository<MyRoutineList, Long> {

    @Query("SELECT DISTINCT mrl FROM MyRoutineList mrl JOIN mrl.routineDays mrd " +
            "WHERE mrl.user = :user " +
            "AND mrl.startDate <= :date " +
            "AND mrd.dayType = :day")
    Page<MyRoutineList> findByUserAndStartDateAfterAndDay(User user, DayType day, LocalDate date, Pageable pageable
    );



    @Query("SELECT m FROM MyRoutineList m WHERE m.user = :user AND m.routineType = :routineType")
    List<MyRoutineList> findAllByUserAndRoutineType(User user, RoutineType routineType);

    @Query("SELECT mrl FROM MyRoutineList mrl JOIN mrl.routineDays mrd WHERE mrl.user = :user AND mrd.dayType = :day")
    List<MyRoutineList> findAllByUserAndDay(@Param("user") User user, @Param("day") DayType day);

    Optional<MyRoutineList> findFirstByUserOrderByIdDesc(User user);
}