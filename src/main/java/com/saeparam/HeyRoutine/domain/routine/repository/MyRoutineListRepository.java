package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.dto.response.MyRoutineListResponseDto;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MyRoutineListRepository extends JpaRepository<MyRoutineList, Long> {

    @Query("SELECT DISTINCT mrl FROM MyRoutineList mrl JOIN mrl.routineDays mrd " +
            "WHERE mrl.user = :user " +
            "AND mrl.startDate <= :localDateTime " +
            "AND mrd.dayType = :day")
    List<MyRoutineList> findByUserAndStartDateAfterAndDay(User user, DayType day, LocalDateTime localDateTime, Pageable pageable
    );
}