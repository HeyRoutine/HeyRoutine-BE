package com.saeparam.HeyRoutine.domain.analysis.service;


import com.saeparam.HeyRoutine.domain.analysis.dto.response.MaxStreakResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.WeeklySummaryDto;
import com.saeparam.HeyRoutine.domain.routine.entity.*;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import com.saeparam.HeyRoutine.domain.routine.repository.*;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    // 필요한 리포지토리들을 주입받습니다.
    private final UserRepository userRepository;
    private final MyRoutineListRepository myRoutineListRepository;
    private final GroupRoutineListRepository groupRoutineListRepository;
    private final MyRoutineListRecordRepository myRoutineListRecordRepository;
    private final GroupRoutineListDoneCheckRepository groupRoutineListDoneCheckRepository;

    /**
     * 주간 요약 정보를 조회하는 메서드입니다.
     * @param userId 조회할 사용자의 ID
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @return 주간 요약 DTO 리스트
     */
    public List<WeeklySummaryDto> getWeeklySummaries(UUID userId, LocalDate startDate, LocalDate endDate, RoutineType routineType) {
        // 1. 사용자 엔티티를 조회합니다. 없으면 예외를 발생시킵니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 사용자가 설정한 모든 개인 루틴 목록과 참여 중인 모든 그룹 루틴 목록을 가져옵니다.
        List<MyRoutineList> myRoutines = myRoutineListRepository.findAllByUserAndRoutineType(user,routineType);
        List<GroupRoutineList> groupRoutines = groupRoutineListRepository.findAllByUserInAndRoutineType(user,routineType);

        // 3. N+1 문제를 방지하기 위해, 해당 기간의 모든 완료 기록을 DB에서 한 번의 쿼리로 미리 조회합니다.
        List<MyRoutineListRecord> myRecords = myRoutineListRecordRepository.findByUserAndCreatedDateBetween(user, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        List<GroupRoutineListDoneCheck> groupRecords = groupRoutineListDoneCheckRepository.findByUserAndCreatedDateBetween(user, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        // 4. 조회 성능을 높이기 위해, 완료된 기록들을 Map 형태로 가공합니다.
        // Key: 루틴 목록 ID, Value: 해당 루틴을 완료한 날짜들의 Set
        Map<Long, Set<LocalDate>> myCompletedMap = myRecords.stream()
//                .filter(MyRoutineListRecord::isDoneCheck) // 완료된 기록만 필터링
                .collect(Collectors.groupingBy(r -> r.getMyRoutineList().getId(), // 루틴 목록 ID로 그룹화
                        Collectors.mapping(r -> r.getCreatedDate().toLocalDate(), Collectors.toSet()))); // 날짜만 추출하여 Set으로 만듦

        Map<Long, Set<LocalDate>> groupCompletedMap = groupRecords.stream()
//                .filter(GroupRoutineListDoneCheck::isDoneCheck)
                .collect(Collectors.groupingBy(r -> r.getGroupRoutineList().getId(),
                        Collectors.mapping(r -> r.getCreatedDate().toLocalDate(), Collectors.toSet())));

        // 5. 최종적으로 반환할 주간 요약 DTO 리스트를 생성합니다.
        List<WeeklySummaryDto> summaries = new ArrayList<>();

        // 5-1. 개인 루틴에 대한 요약 정보를 생성합니다.
        myRoutines.forEach(routine -> {
            Map<DayType, Boolean> dailyStatus = new EnumMap<>(DayType.class); // 요일 순서를 보장하기 위해 EnumMap 사용
            // 시작일부터 종료일까지 하루씩 순회합니다.
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                // 미리 만들어둔 Map에서 해당 날짜에 완료 기록이 있는지 O(1) 시간 복잡도로 빠르게 확인합니다.
                boolean isCompleted = myCompletedMap.getOrDefault(routine.getId(), Collections.emptySet()).contains(date);
                dailyStatus.put(DayType.from(date.getDayOfWeek()), isCompleted);
            }
            summaries.add(new WeeklySummaryDto(routine.getTitle(), dailyStatus));
        });

        // 5-2. 그룹 루틴에 대한 요약 정보를 생성합니다.
        groupRoutines.forEach(routine -> {
            Map<DayType, Boolean> dailyStatus = new EnumMap<>(DayType.class);
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                boolean isCompleted = groupCompletedMap.getOrDefault(routine.getId(), Collections.emptySet()).contains(date);
                dailyStatus.put(DayType.from(date.getDayOfWeek()), isCompleted);
            }
            summaries.add(new WeeklySummaryDto(routine.getTitle(), dailyStatus));
        });

        return summaries;
    }

    /**
     * 최대 연속 달성일을 계산하는 메서드입니다.
     * @param userId 조회할 사용자의 ID
     * @return 최대 연속 달성 정보 DTO
     */
    public MaxStreakResponseDto calculateMaxStreak(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        int currentStreak = 1;
        LocalDate checkDate = LocalDate.now().minusDays(1); // 어제부터 거꾸로 확인 시작

        // 무한 루프를 돌며 하루씩 과거로 이동합니다.
        while (true) {
            DayType dayOfWeek = DayType.from(checkDate.getDayOfWeek());

            // 1. 확인하려는 날짜(checkDate)에 '수행하도록 설정된' 루틴 목록이 하나라도 있는지 확인합니다.
            boolean isRoutineScheduled = !myRoutineListRepository.findAllByUserAndDay(user, dayOfWeek).isEmpty() ||
                    !groupRoutineListRepository.findAllByUserAndDay(user, dayOfWeek).isEmpty();

            // 2. 만약 그날에 예정된 루틴이 아예 없었다면, 연속 기록에 영향을 주지 않고 그냥 건너뜁니다.
            if (!isRoutineScheduled) {
                currentStreak = 0;
                checkDate = checkDate.minusDays(1); // 다음 날(과거)로 이동
                // 너무 과거 데이터까지 확인하는 것을 방지하기 위한 안전장치 (예: 1년)
                if (checkDate.isBefore(LocalDate.now().minusYears(1))) break;
                continue; // 아래 로직을 실행하지 않고 다음 루프 시작
            }

            // 3. 그날의 '실제 수행 완료 기록'을 DB에서 가져옵니다.
            List<MyRoutineListRecord> myRecords = myRoutineListRecordRepository.findByUserAndCreatedDateBetween(user, checkDate.atStartOfDay(), checkDate.plusDays(1).atStartOfDay());
            List<GroupRoutineListDoneCheck> groupRecords = groupRoutineListDoneCheckRepository.findByUserAndCreatedDateBetween(user, checkDate.atStartOfDay(), checkDate.plusDays(1).atStartOfDay());

            // 4. 그날 완료한 개인 또는 그룹 루틴이 '하나라도 있는지' 확인합니다.
            boolean hasCompletedAnyRoutine = myRecords.stream().anyMatch(MyRoutineListRecord::isDoneCheck) ||
                    groupRecords.stream().anyMatch(GroupRoutineListDoneCheck::isDoneCheck);

            // 5. 예정된 루틴이 있었고, 그 중 하나라도 완료했다면 성공으로 간주합니다.
            if (hasCompletedAnyRoutine) {
                // 연속 달성일을 1 증가시키고 다음 날(과거)로 이동합니다.
                currentStreak++;
                checkDate = checkDate.minusDays(1);
            } else {
                // 예정된 루틴이 있었지만 하나도 완료하지 못했다면, 연속 기록이 깨진 것이므로 루프를 중단합니다.
                break;
            }

            if (checkDate.isBefore(LocalDate.now().minusYears(1))) break;
        }

        return new MaxStreakResponseDto(currentStreak);
    }

}