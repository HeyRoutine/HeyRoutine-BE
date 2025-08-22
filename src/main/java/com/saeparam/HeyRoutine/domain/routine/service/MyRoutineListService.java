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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
    private final RoutineRecordRepository routineRecordRepository;
    private final MyRoutineListRecordRepository myRoutineListRecordRepository;




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

    @Transactional
    public String completeRoutine(UUID userId, Long routineId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.ROUTINE_NOT_FOUND));

        // 루틴의 소유권이 현재 사용자와 일치하는지 확인
        if(routine.getRoutineMiddles().isEmpty() || !routine.getRoutineMiddles().get(0).getRoutineList().getUser().equals(user)){
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Optional<RoutineRecord> recordOpt = routineRecordRepository.findRecordByDateAndRoutine(user, routine, startOfDay, endOfDay);

        if (recordOpt.isEmpty()) {
            RoutineRecord newRecord = RoutineRecord.builder()
                    .user(user)
                    .routine(routine)
                    .doneCheck(true)
                    .build();
            routineRecordRepository.save(newRecord);
        }


        return "루틴이 완료 처리되었습니다.";
    }


    //새로운 개인루틴안 루틴보기
    @Transactional(readOnly = true)
    public List<RoutineResponseDto> getRoutinesInListByDate(UUID userId, Long routineListId, LocalDate date) {
        // 1. 사용자 정보를 조회합니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 루틴 목록을 조회하고, 소유권이 현재 사용자와 일치하는지 확인합니다.
        MyRoutineList routineList = myRoutineListRepository.findById(routineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.MY_ROUTINE_LIST_NOT_FOUND));

        if (!routineList.getUser().equals(user)) {
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }

        // 3. 루틴 목록에 속한 모든 루틴들을 연관관계를 통해 가져옵니다.
        List<Routine> routines = routineList.getRoutineMiddles().stream()
                .map(middle -> middle.getRoutine())
                .collect(Collectors.toList());

        if (routines.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 4. 추출된 루틴들을 사용하여, 특정 날짜의 수행 기록(RoutineRecord)들을 DB에서 한 번에 조회합니다.
        List<RoutineRecord> records = routineRecordRepository.findRecordsByDateAndRoutines(user, startOfDay, endOfDay, routines);

        // 5. 조회된 기록 중, 완료된(done_check=true) 루틴의 ID만 Set으로 만들어 빠른 조회를 준비합니다.
        Set<Long> completedRoutineIds = records.stream()
                .filter(RoutineRecord::isDoneCheck)
                .map(record -> record.getRoutine().getId())
                .collect(Collectors.toSet());

        // 6. 최종적으로, 루틴 정보와 그날의 수행 여부를 조합하여 DTO 리스트로 만듭니다.
        return routines.stream()
                .map(routine -> {
                    boolean isCompleted = completedRoutineIds.contains(routine.getId());
                    return RoutineResponseDto.toDto(routine, isCompleted);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public String completeMyRoutineList(UUID userId, Long myRoutineListId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        MyRoutineList routineList = myRoutineListRepository.findById(myRoutineListId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.MY_ROUTINE_LIST_NOT_FOUND));

        // 루틴 목록의 소유권 확인
        if (!routineList.getUser().equals(user)) {
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 해당 날짜에 이미 완료 기록이 있는지 확인
        Optional<MyRoutineListRecord> recordOpt = myRoutineListRecordRepository
                .findByUserAndMyRoutineListAndCreatedDateBetween(user, routineList, startOfDay, endOfDay);

        // 기록이 없을 경우에만 새로 생성 (중복 방지)
        if (recordOpt.isEmpty()) {
            MyRoutineListRecord newRecord = MyRoutineListRecord.builder()
                    .user(user)
                    .myRoutineList(routineList)
                    .doneCheck(true)
                    .build();
            myRoutineListRecordRepository.save(newRecord);
        }

        return "루틴 목록이 완료 처리되었습니다.";

    }
}
