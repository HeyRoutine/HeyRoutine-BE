package com.saeparam.HeyRoutine.domain.routine.service;


import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListMakeRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.MyRoutineListResponseDto;
import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineDays;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.repository.EmojiRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.MyRoutineDaysRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.MyRoutineListRepository;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmojiService {

    private final EmojiRepository emojiRepository;

    @Transactional(readOnly = true)
    public List<Emoji> showEmoji(Pageable pageable) {
        return emojiRepository.findAll(pageable).stream().toList();
    }
}
