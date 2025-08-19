package com.saeparam.HeyRoutine.domain.routine.service;


import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListMakeRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.MyRoutineListResponseDto;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineDays;
import com.saeparam.HeyRoutine.domain.routine.entity.MyRoutineList;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.repository.MyRoutineDaysRepository;
import com.saeparam.HeyRoutine.domain.routine.repository.MyRoutineListRepository;
import com.saeparam.HeyRoutine.domain.shop.dto.request.PointShopPostRequestDto;
import com.saeparam.HeyRoutine.domain.shop.dto.response.PointShopDetailResponseDto;
import com.saeparam.HeyRoutine.domain.shop.dto.response.PointShopListResponseDto;
import com.saeparam.HeyRoutine.domain.shop.entity.PointShop;
import com.saeparam.HeyRoutine.domain.shop.enums.PointShopCategory;
import com.saeparam.HeyRoutine.domain.shop.repository.PointShopRepository;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.common.aop.DistributedLock;
import com.saeparam.HeyRoutine.global.error.handler.ShopHandler;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyRoutineListService {

    private final MyRoutineListRepository myRoutineListRepository;
    private final UserRepository userRepository;
    private final MyRoutineDaysRepository myRoutineDaysRepository;

    @Transactional
    public String makeMyRoutineList(String email, MyRoutineListMakeRequestDto myRoutineListMakeRequestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        MyRoutineList myRoutineList = MyRoutineListMakeRequestDto.toEntity(myRoutineListMakeRequestDto, user);
        myRoutineListRepository.save(myRoutineList);
        for (DayType day : myRoutineListMakeRequestDto.getDayTypes()) {
            myRoutineDaysRepository.save(MyRoutineDays.builder()
                    .routineList(myRoutineList)
                    .dayType(day)
                    .build());
        }

        return "리스트가 저장되었습니다";
    }

    public List<MyRoutineListResponseDto> showMyRoutineList(String email, DayType day, LocalDateTime localDateTime,Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        List<MyRoutineList> myRoutineList=myRoutineListRepository.findByUserAndStartDateAfterAndDay(user,day,localDateTime,pageable);

        return myRoutineList.stream().map(MyRoutineListResponseDto::toDto).collect(Collectors.toList());
    }
}
