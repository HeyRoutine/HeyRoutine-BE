package com.saeparam.HeyRoutine.domain.routine.service;


import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.RoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.MyRoutineListResponseDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.RoutineResponseDto;
import com.saeparam.HeyRoutine.domain.routine.entity.*;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.repository.*;

import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;

import com.saeparam.HeyRoutine.global.error.handler.RoutineHandler;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyRoutineListService {

    private final MyRoutineListRepository myRoutineListRepository;
    private final UserRepository userRepository;
    private final MyRoutineDaysRepository myRoutineDaysRepository;
    private final MyRoutineMiddleRepository myRoutineMiddleRepository;
    private final EmojiRepository emojiRepository;
    private final RoutineRepository routineRepository;




    @Transactional
    public MyRoutineListResponseDto makeMyRoutineList(UUID userID, MyRoutineListRequestDto myRoutineListRequestDto) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        MyRoutineList myRoutineList = MyRoutineListRequestDto.toEntity(myRoutineListRequestDto, user);


        myRoutineListRepository.save(myRoutineList);
        Set<DayType> dayTypeSet=new HashSet<>();
        for (DayType day : myRoutineListRequestDto.getDayTypes()) {
            myRoutineDaysRepository.save(MyRoutineDays.builder()
                    .routineList(myRoutineList)
                    .dayType(day)
                    .build());
            dayTypeSet.add(day);
        }

        MyRoutineListResponseDto myRoutineListResponseDto=MyRoutineListResponseDto.toDto(myRoutineList);
        myRoutineListResponseDto.setDayTypes(dayTypeSet);
        return myRoutineListResponseDto;

    }

    @Transactional(readOnly = true)
    public List<MyRoutineListResponseDto> showMyRoutineList(UUID userId, DayType day, LocalDateTime localDateTime,Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        List<MyRoutineList> myRoutineList=myRoutineListRepository.findByUserAndStartDateAfterAndDay(user,day,localDateTime,pageable);

        return myRoutineList.stream().map(MyRoutineListResponseDto::toDto).collect(Collectors.toList());
    }

    @Transactional
    public String makeRoutineInMyRoutineList(UUID userId, Long id, RoutineRequestDto routineRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        MyRoutineList myRoutineList=myRoutineListRepository.findById(id)
                .orElseThrow(()->new RoutineHandler(ErrorStatus.MY_ROUTINE_LIST_NOT_FOUND));
        // 루틴리스트 권한 확인
        if (!myRoutineList.getUser().equals(user)){
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        Emoji emoji=emojiRepository.findById(routineRequestDto.getEmojiId())
                .orElseThrow(()->new RoutineHandler(ErrorStatus.EMOJI_NOT_FOUND));
        Routine routine=routineRepository.save(RoutineRequestDto.toEntity(routineRequestDto,emoji));
        myRoutineMiddleRepository.save(MyRoutineMiddle.builder()
                        .routineList(myRoutineList)
                        .routine(routine)
                .build());

        return "루틴이 저장되었습니다";
    }

    @Transactional(readOnly = true)
    public List<RoutineResponseDto> showRoutineInMyRoutineList(UUID userId, Long id) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        MyRoutineList myRoutineList=myRoutineListRepository.findById(id)
                .orElseThrow(()->new RoutineHandler(ErrorStatus.MY_ROUTINE_LIST_NOT_FOUND));
        if (!myRoutineList.getUser().equals(user)) {
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        List<Routine> routines = routineRepository.findAllByRoutineListId(id);

        return routines.stream().map(RoutineResponseDto::toDto).collect(Collectors.toList());
    }

    @Transactional
    public String updateMyRoutineList(UUID userId, Long id, MyRoutineListRequestDto myRoutineListRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        MyRoutineList myRoutineList=myRoutineListRepository.findById(id)
                .orElseThrow(()->new RoutineHandler(ErrorStatus.MY_ROUTINE_LIST_NOT_FOUND));
        if (!myRoutineList.getUser().equals(user)) {
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        myRoutineList.update(myRoutineListRequestDto);
        return "수정 됐습니다.";


    }

    @Transactional
    public String deleteMyRoutineList(UUID userId, Long id) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        MyRoutineList myRoutineList=myRoutineListRepository.findById(id)
                .orElseThrow(()->new RoutineHandler(ErrorStatus.MY_ROUTINE_LIST_NOT_FOUND));
        if (!myRoutineList.getUser().equals(user)) {
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        myRoutineListRepository.delete(myRoutineList);

        return "삭제 됐습니다.";
    }

    @Transactional
    public String updateInMyRoutineList(UUID userId, Long id, RoutineRequestDto routineRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        Routine routine=routineRepository.findById(id)
                .orElseThrow(()->new RoutineHandler(ErrorStatus.ROUTINE_NOT_FOUND));
        Emoji emoji=emojiRepository.findById(routineRequestDto.getEmojiId())
                .orElseThrow(()->new RoutineHandler(ErrorStatus.EMOJI_NOT_FOUND));
        if(!routine.getRoutineMiddles().get(0).getRoutineList().getUser().equals(user)){
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        routine.update(routineRequestDto,emoji);

        return "루틴이 수정되었습니다.";
    }

    @Transactional
    public String deleteInMyRoutineList(UUID userId, Long routineId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        Routine routine=routineRepository.findById(routineId)
                .orElseThrow(()->new RoutineHandler(ErrorStatus.ROUTINE_NOT_FOUND));
        if(!routine.getRoutineMiddles().get(0).getRoutineList().getUser().equals(user)){
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        routineRepository.delete(routine);
        return "루틴이 삭제되었습니다.";
    }
}
