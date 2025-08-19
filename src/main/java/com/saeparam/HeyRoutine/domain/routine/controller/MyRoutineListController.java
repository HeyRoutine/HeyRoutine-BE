package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListMakeRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.MyRoutineListResponseDto;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.service.MyRoutineListService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/my-routine")
public class MyRoutineListController {

    private final MyRoutineListService myRoutineListService;
    private final JwtTokenProvider jwtTokenProvider;
    @PostMapping("/list")
    public ResponseEntity<?> makeMyRoutineList(@RequestHeader("Authorization") String token,@RequestBody MyRoutineListMakeRequestDto myRoutineListMakeRequestDto){
        String email = jwtTokenProvider.getEmail(token.substring(7));

        String result=myRoutineListService.makeMyRoutineList(email,myRoutineListMakeRequestDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    @GetMapping("/list")
    public ResponseEntity<?> showMyRoutineList(@RequestHeader("Authorization") String token, @RequestParam DayType day, @RequestParam LocalDateTime localDateTime, @PageableDefault(size = 10, sort = "createdDate",direction = Sort.Direction.DESC) Pageable pageable){
        String email = jwtTokenProvider.getEmail(token.substring(7));

        List<MyRoutineListResponseDto> result=myRoutineListService.showMyRoutineList(email,day,localDateTime,pageable);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }
}
