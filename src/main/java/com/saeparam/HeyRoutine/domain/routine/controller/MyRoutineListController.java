package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListMakeRequestDto;
import com.saeparam.HeyRoutine.domain.routine.service.MyRoutineListService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
