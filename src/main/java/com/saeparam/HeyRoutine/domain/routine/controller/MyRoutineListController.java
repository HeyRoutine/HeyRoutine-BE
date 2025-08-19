package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListMakeRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.RoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.MyRoutineListResponseDto;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.service.MyRoutineListService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    /**
     * 개인루틴 리스트 만들기
     */
    @PostMapping("/list")
    @Operation(summary = "개인루틴 리스트 만들기 API", description = "개인루틴 리스트를 만듭니다.")
    public ResponseEntity<?> makeMyRoutineList(@RequestHeader("Authorization") String token,@RequestBody MyRoutineListMakeRequestDto myRoutineListMakeRequestDto){
        String email = jwtTokenProvider.getEmail(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.makeMyRoutineList(email,myRoutineListMakeRequestDto)));
    }

    /**
     * 모든 개인루틴 리스트 보여주기
     * 페이지네이션 10개씩
     * 정렬 및 정제후 반환함
     */
    @GetMapping("/list")
    @Operation(summary = "개인루틴 리스트 전체조회 API", description = "개인루틴 리스트 전체를 반환합니다 페이지네이션 10개씩 반환 정렬 및 정제후 반환. (시작시간과 끝시간 사이의 값이 아니면 실행 못하게 막아야할듯 프론트 부탁 !)")
    public ResponseEntity<?> showMyRoutineList(@RequestHeader("Authorization") String token, @RequestParam DayType day, @RequestParam LocalDateTime localDateTime, @PageableDefault(size = 10, sort = "createdDate",direction = Sort.Direction.DESC) Pageable pageable){
        String email = jwtTokenProvider.getEmail(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.showMyRoutineList(email,day,localDateTime,pageable)));
    }

    /**
     * 개인루틴 리스트 안 루틴 만들기
     */
    @PostMapping("/list/{id}")
    @Operation(summary = "개인루틴 리스트안에 루틴만들기 API", description = "개인루틴 리스트안에 루틴을 만듭니다.")
    public ResponseEntity<?> makeRoutineToMyRoutineList(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody RoutineRequestDto routineRequestDto){
        String email = jwtTokenProvider.getEmail(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.makeRoutineToMyRoutineList(email,id,routineRequestDto)));
    }

    //TODO 개인루틴 리스트 안 루틴 모두 보기
}
