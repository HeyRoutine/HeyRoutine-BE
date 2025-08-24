package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.dto.request.MyRoutineListRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.RoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.RoutineResponseDto;
import com.saeparam.HeyRoutine.domain.routine.enums.DayType;
import com.saeparam.HeyRoutine.domain.routine.service.MyRoutineListService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<?> makeMyRoutineList(@RequestHeader("Authorization") String token,@RequestBody MyRoutineListRequestDto myRoutineListRequestDto){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.makeMyRoutineList(userId, myRoutineListRequestDto)));
    }

    /**
     * 개인루틴 리스트 수정
     * id = 개인루틴리스트 id
     */
    @PatchMapping("/list/{myRoutineListId}")
    @Operation(summary = "개인루틴 리스트 수정 API", description = "개인루틴 리스트를 수정합니다.")
    public ResponseEntity<?> updateRoutineToMyRoutineList(@RequestHeader("Authorization") String token, @PathVariable Long myRoutineListId, @RequestBody MyRoutineListRequestDto myRoutineListRequestDto){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.updateMyRoutineList(userId,myRoutineListId, myRoutineListRequestDto)));
    }

    /**
     * 개인루틴 리스트 삭제
     * id = 개인루틴리스트 id
     */
    @DeleteMapping("/list/{myRoutineListId}")
    @Operation(summary = "개인루틴 리스트 삭제 API", description = "개인루틴 리스트를 삭제합니다.")
    public ResponseEntity<?> deleteRoutineToMyRoutineList(@RequestHeader("Authorization") String token, @PathVariable Long myRoutineListId){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.deleteMyRoutineList(userId,myRoutineListId)));
    }

    /**
     * 모든 개인루틴 리스트 보여주기
     * 페이지네이션 10개씩
     * 정렬 및 정제후 반환함
     */
    @GetMapping("/list")
    @Operation(summary = "개인루틴 리스트 전체조회 API", description = "개인루틴 리스트 전체를 반환합니다 페이지네이션 10개씩 반환 정렬 및 정제후 반환. (시작시간과 끝시간 사이의 값이 아니면 실행 못하게 막아야할듯 프론트 부탁 !)")
    public ResponseEntity<?> showMyRoutineList(@RequestHeader("Authorization") String token, @RequestParam DayType day, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PageableDefault(size = 10, sort = "createdDate",direction = Sort.Direction.DESC) Pageable pageable){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.showMyRoutineList(userId,day,date,pageable)));
    }

    /**
     * 개인루틴 리스트 안 루틴 만들기
     * id = 개인루틴리스트 id
     */
    @PostMapping("/list/routine/{myRoutineListId}")
    @Operation(summary = "개인루틴 리스트안에 루틴만들기 API", description = "개인루틴 리스트안에 루틴을 만듭니다.")
    public ResponseEntity<?> makeRoutineToMyRoutineList(@RequestHeader("Authorization") String token, @PathVariable Long myRoutineListId, @RequestBody List<RoutineRequestDto> routineRequestDtoList){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.makeRoutineInMyRoutineList(userId,myRoutineListId,routineRequestDtoList)));
    }

    @GetMapping("/list/routine/{myRoutineListId}")
    @Operation(summary = "특정 날짜의 루틴 목록 상세 조회 API", description = "루틴 목록에 포함된 모든 루틴과 해당 날짜의 수행 여부를 함께 조회합니다.")
    public ResponseEntity<?> getRoutinesInListByDate(
            @RequestHeader("Authorization") String token,
            @PathVariable Long myRoutineListId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        List<RoutineResponseDto> result = myRoutineListService.getRoutinesInListByDate(userId, myRoutineListId, date);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    /**
     * 개인루틴 리스트 안 전체 루틴 수정
     * id = 루틴Id
     */
    @PatchMapping("/list/routine/{routineId}")
    @Operation(summary = "개인루틴 리스트 안 루틴 수정 API", description = "개인루틴 리스트 안 루틴 수정")
    public ResponseEntity<?> updateRoutineInMyRoutineList(@RequestHeader("Authorization") String token,@PathVariable Long routineId,@RequestBody RoutineRequestDto routineRequestDto){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.updateInMyRoutineList(userId,routineId,routineRequestDto)));
    }

    /**
     * 개인루틴 리스트 안 루틴 삭제
     * id = 루틴Id
     */
    @DeleteMapping("/list/routine/{routineId}")
    @Operation(summary = "개인루틴 리스트 안 루틴 삭제 API", description = "개인루틴 리스트 안 루틴 삭제")
    public ResponseEntity<?> deleteRoutineInMyRoutineList(@RequestHeader("Authorization") String token,@PathVariable Long routineId){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.deleteInMyRoutineList(userId,routineId)));
    }

    /**
     * 루틴 기록하기
     * id = 루틴Id
     */
    @PostMapping("/list/routine/complete/{routineId}")
    @Operation(summary = "개인루틴 리스트안 루틴 수행 API", description = "개인루틴 리스트안 루틴을 수행완료 합니다")
    public ResponseEntity<?> doneRoutineToMyRoutineList(@RequestHeader("Authorization") String token, @PathVariable Long routineId,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.completeRoutine(userId,routineId,date)));
    }

    /**
     * 루틴리스트 기록하기
     * id = 루틴리스트 Id
     */
    @PostMapping("/list/complete/{myRoutineListId}")
    @Operation(summary = "개인루틴 리스트 기록 API", description = "개인루틴 리스트 수행을 기록 합니다")
    public ResponseEntity<?> doneMyRoutineList(@RequestHeader("Authorization") String token, @PathVariable Long myRoutineListId,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ){
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(myRoutineListService.completeMyRoutineList(userId,myRoutineListId,date)));
    }


}
