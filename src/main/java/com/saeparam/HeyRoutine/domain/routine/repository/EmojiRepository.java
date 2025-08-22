package com.saeparam.HeyRoutine.domain.routine.repository;

import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineDays;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmojiRepository extends JpaRepository<Emoji, Long> {

}