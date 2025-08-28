package com.saeparam.HeyRoutine.domain.routine.service;


import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.RoutineInMyRoutineUpdateRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.RoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.RoutineUpdateRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.MyRoutineListResponseDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.MyRoutineListShowResponseDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.RoutineResponseDto;
import com.saeparam.HeyRoutine.domain.routine.entity.*;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.repository.*;

import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;

import com.saeparam.HeyRoutine.global.error.handler.RoutineHandler;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.PaginatedResponse;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    public PaginatedResponse<MyRoutineListShowResponseDto> showMyRoutineList(UUID userId, DayType day, LocalDate date, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        Page<MyRoutineList> myRoutineList = myRoutineListRepository.findByUserAndStartDateAfterAndDay(user, day, date, pageable);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        List<MyRoutineListRecord> weekRecords = myRoutineListRecordRepository
                .findByUserAndCreatedDateBetween(user, startOfWeek.atStartOfDay(), LocalDateTime.now());

        return PaginatedResponse.of(myRoutineList, list -> {
            // 상세 루틴 목록 조회
            List<MyRoutineMiddle> middles = myRoutineMiddleRepository.findByRoutineList(list);
            List<Routine> routines = middles.stream()
                    .map(MyRoutineMiddle::getRoutine)
                    .collect(Collectors.toList());

            int routineCount = routines.size();
            long doneCount = 0;
            if (!routines.isEmpty()) {
                List<RoutineRecord> records = routineRecordRepository.findRecordsByDateAndRoutines(user, startOfDay, endOfDay, routines);
                doneCount = records.stream()
                        .filter(RoutineRecord::isDoneCheck)
                        .count();
            }
            double percent = routineCount > 0 ? Math.round((double) doneCount * 1000 / routineCount) / 10.0 : 0.0;

            MyRoutineListShowResponseDto dto = MyRoutineListShowResponseDto.toDto(list);
            dto.setPercent(percent);

            List<String> successDay = weekRecords.stream()
                    .filter(record -> record.isDoneCheck() && record.getMyRoutineList().equals(list))
                    .map(record -> DayType.from(record.getCreatedDate().getDayOfWeek()).name())
                    .distinct()
                    .collect(Collectors.toList());
            dto.setSuccessDay(successDay);
            return dto;
        });
    }

    @Transactional
    public String makeRoutineInMyRoutineList(UUID userId, Long id, List<RoutineRequestDto> routineRequestDtoList) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        MyRoutineList myRoutineList=myRoutineListRepository.findById(id)
                .orElseThrow(()->new RoutineHandler(ErrorStatus.MY_ROUTINE_LIST_NOT_FOUND));
        // 루틴리스트 권한 확인
        if (!myRoutineList.getUser().equals(user)){
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        for(RoutineRequestDto routineRequest:routineRequestDtoList) {
            Emoji emoji = emojiRepository.findById(routineRequest.getEmojiId())
                    .orElseThrow(() -> new RoutineHandler(ErrorStatus.EMOJI_NOT_FOUND));
            Routine routine = routineRepository.save(RoutineRequestDto.toEntity(routineRequest, emoji));
            myRoutineMiddleRepository.save(MyRoutineMiddle.builder()
                    .routineList(myRoutineList)
                    .routine(routine)
                    .build());
        }
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
        myRoutineListRecordRepository.deleteByMyRoutineList(myRoutineList);

        myRoutineListRepository.delete(myRoutineList);

        return "삭제 됐습니다.";
    }

    @Transactional
    public String updateInMyRoutineList(UUID userId, Long routineListId, RoutineInMyRoutineUpdateRequestDto routineInMyRoutineUpdateRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        makeRoutineInMyRoutineList(userId,routineListId,routineInMyRoutineUpdateRequestDto.getMakeRoutine());
        for(RoutineUpdateRequestDto routineUpdateRequestDto:routineInMyRoutineUpdateRequestDto.getUpdateRoutine()) {
            Routine routine = routineRepository.findById(routineUpdateRequestDto.getId())
                    .orElseThrow(() -> new RoutineHandler(ErrorStatus.SUB_ROUTINE_NOT_FOUND));
            if (!routine.getRoutineMiddles().getRoutineList().getUser().equals(user)) {
                throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
            }
            Emoji emoji = emojiRepository.findById(routineUpdateRequestDto.getEmojiId())
                    .orElseThrow(() -> new RoutineHandler(ErrorStatus.EMOJI_NOT_FOUND));

            routine.update(routineUpdateRequestDto, emoji);
        }
        return "루틴이 수정되었습니다.";
    }

    @Transactional
    public String deleteInMyRoutineList(UUID userId, Long routineId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        Routine routine=routineRepository.findById(routineId)
                .orElseThrow(()->new RoutineHandler(ErrorStatus.SUB_ROUTINE_NOT_FOUND));
        if(!routine.getRoutineMiddles().getRoutineList().getUser().equals(user)){
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        // 루틴기록 삭제
        routineRepository.delete(routine);
        return "루틴이 삭제되었습니다.";
    }

    @Transactional
    public String completeRoutine(UUID userId, Long routineId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineHandler(ErrorStatus.SUB_ROUTINE_NOT_FOUND));

        // 루틴의 소유권이 현재 사용자와 일치하는지 확인
        MyRoutineMiddle myRoutineMiddle=routine.getRoutineMiddles();
        if(myRoutineMiddle==null || !myRoutineMiddle.getRoutineList().getUser().equals(user)){
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
            newRecord.setCreatedDate(startOfDay);
            newRecord.setModifiedDate(startOfDay);
            routineRecordRepository.save(newRecord);
        }
        MyRoutineList routineList = myRoutineMiddle.getRoutineList();
        checkAndCompleteRoutineList(user, routineList, date);



        return "루틴이 완료 처리되었습니다.";
    }


    /**
     * [추가된 헬퍼 메서드]
     * 특정 루틴 리스트의 모든 루틴이 완료되었는지 확인하고, 그렇다면 리스트 자체를 완료 처리합니다.
     */
    private void checkAndCompleteRoutineList(User user, MyRoutineList routineList, LocalDate date) {
        // 1. 해당 루틴 리스트에 포함된 모든 루틴들을 가져옵니다.
        List<Routine> allRoutinesInList = routineList.getRoutineMiddles().stream()
                .map(middle -> middle.getRoutine())
                .collect(Collectors.toList());

        if (allRoutinesInList.isEmpty()) {
            return; // 루틴이 없는 리스트는 처리하지 않음
        }

        // 2. 오늘 완료된 루틴의 개수를 DB에서 직접 셉니다.
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        long completedCount = routineRecordRepository.countCompletedRoutinesInList(user, startOfDay, endOfDay, allRoutinesInList);

        System.out.println(allRoutinesInList.size()+"리스트 개수");
        System.out.println(completedCount+"성공 개수");
        // 3. 전체 루틴 개수와 완료된 루틴 개수가 같은지 비교합니다.
        if (allRoutinesInList.size() == completedCount) {
            // 4. 모든 루틴이 완료되었다면, MyRoutineListRecord에 기록을 남깁니다.
            //    이미 기록이 있는지 확인하여 중복 저장을 방지합니다.
            Optional<MyRoutineListRecord> recordOpt = myRoutineListRecordRepository
                    .findByUserAndMyRoutineListAndCreatedDateBetween(user, routineList, startOfDay, endOfDay);

            // 기록이 없을 경우에만 새로 생성합니다.
            if (recordOpt.isEmpty()) {
                MyRoutineListRecord newListRecord = MyRoutineListRecord.builder()
                        .user(user)
                        .myRoutineList(routineList)
                        .doneCheck(true)
                        .createdDate(startOfDay)
                        .modifiedDate(startOfDay)
                        .build();

                myRoutineListRecordRepository.save(newListRecord);
            }
        }
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
                    .createdDate(startOfDay)
                    .modifiedDate(startOfDay)
                    .build();
            myRoutineListRecordRepository.save(newRecord);
        }

        return "루틴 목록이 완료 처리되었습니다.";

    }
}
