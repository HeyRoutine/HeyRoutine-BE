package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.GroupRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRoutineListRepository extends JpaRepository<GroupRoutineList, Long> {
    @Query("SELECT grl FROM GroupRoutineList grl JOIN grl.userInRooms uir WHERE uir.user = :user and grl.routineType=:routineType")
    List<GroupRoutineList> findAllByUserInAndRoutineType(@Param("user") User user, RoutineType routineType);
    @Query("SELECT grl FROM GroupRoutineList grl " +
            "JOIN grl.userInRooms uir " +
            "JOIN grl.groupRoutineDays grd " +
            "WHERE uir.user = :user AND grd.dayType = :day")
    List<GroupRoutineList> findAllByUserAndDay(@Param("user") User user, @Param("day") DayType day);

}