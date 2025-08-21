package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.dto.request.GroupRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.GuestbookRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.request.SubRoutineRequestDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.GroupRoutineResponseDto;
import com.saeparam.HeyRoutine.domain.routine.dto.response.GuestbookResponseDto;
import com.saeparam.HeyRoutine.domain.routine.service.GroupRoutineService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import com.saeparam.HeyRoutine.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;



/**
 * <h2>GroupRoutineController</h2>
 * <p>
 * 단체 루틴(Group Routine) 관련 API 요청을 처리하는 컨트롤러입니다. <br>
 * 클라이언트의 HTTP 요청을 받아 비즈니스 로직을 {@link GroupRoutineService}에 위임하고, 처리 결과를 {@link ApiResponse} 형태로 반환합니다.
 * </p>
 *
 * <pre>
 * ┌────────────────┐      ┌──────────────────────────┐      ┌─────────────────────┐
 * │    Client      │◄───► │  GroupRoutineController  │◄───►│  GroupRoutineService  │
 * └────────────────┘      └──────────────────────────┘      └─────────────────────┘
 * </pre>
 *
 * <h3>담당 API 목록</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────────────────────────────────┐
 * │                                       API Endpoints                                       │
 * ├───────────────────────────────────────────────────────────────────────────────────────────┤
 * │ [GET]    /api/v1/routines/groups           : 단체 루틴 목록 조회 (페이징)                     │
 * │ [POST]   /api/v1/routines/groups           : 단체 루틴 생성                                 │
 * │ [GET]    /api/v1/routines/groups/{id}      : 단체 루틴 상세 조회                             │
 * │ [PUT]    /api/v1/routines/groups/{id}      : 단체 루틴 수정                                 │
 * │ [DELETE] /api/v1/routines/groups/{id}      : 단체 루틴 삭제                                 │
 * │ [POST]   /api/v1/routines/groups/{id}/join : 단체 루틴 참여                                 │
 * │                                                                                           │
 * │ [POST]   /api/v1/routines/groups/{id}/sub-routines : 상세 루틴 생성                         │
 * │ [PUT]    /api/v1/routines/groups/{id}/sub-routines : 상세 루틴 수정                         │
 * │ [DELETE] /api/v1/routines/groups/{id}/sub-routines/{routineId} : 상세 루틴 삭제             │
 * │ [PATCH]  /api/v1/routines/groups/{id}/status/{routineId} : 상세 루틴 상태 변경               │
 * │                                                                                           │
 * │ [GET]    /api/v1/routines/groups/{id}/guestbooks : 방명록 조회 (페이징)                      │
 * │ [POST]   /api/v1/routines/groups/{id}/guestbooks : 방명록 작성                              │
 * │ [DELETE] /api/v1/routines/groups/{id}/guestbooks/{guestbookId} : 방명록 삭제                │
 * └───────────────────────────────────────────────────────────────────────────────────────────┘
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routines/groups")
@Tag(name = "Routine-Group", description = "단체 루틴 관련 API")
public class GroupRoutineController {

    private final GroupRoutineService groupRoutineService;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping
    @Operation(summary = "단체루틴 리스트 조회 API", description = "모든 단체루틴 리스트를 페이지네이션으로 조회합니다.")
    public ResponseEntity<ApiResponse<GroupRoutineResponseDto.ListResponse>> getGroupRoutines(@RequestHeader("Authorization") String token,
                                                                                              Pageable pageable) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        GroupRoutineResponseDto.ListResponse response = groupRoutineService.getGroupRoutines(uuid, pageable);
        if (response.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.noContent());
        }
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }


    @PostMapping
    @Operation(summary = "단체루틴 생성 API", description = "새로운 단체루틴을 생성합니다.")
    public ResponseEntity<ApiResponse<Void>> createGroupRoutine(@RequestHeader("Authorization") String token,
                                                                @Valid @RequestBody GroupRoutineRequestDto.Create createDto) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.createGroupRoutine(uuid, createDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.INSERT_SUCCESS.getMessage()));
    }


    @GetMapping("/{groupRoutineListId}")
    @Operation(summary = "단체루틴 상세 조회 API", description = "특정 단체루틴의 상세 정보를 조회합니다. 사용자의 참여 여부에 따라 다른 정보가 반환됩니다.")
    public ResponseEntity<ApiResponse<GroupRoutineResponseDto.DetailResponse>> getGroupRoutineDetail(@RequestHeader("Authorization") String token,
                                                                                                     @PathVariable Long groupRoutineListId) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        GroupRoutineResponseDto.DetailResponse response = groupRoutineService.getGroupRoutineDetail(uuid, groupRoutineListId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response, SuccessStatus.SELECT_SUCCESS.getMessage()));
    }


    @PutMapping("/{groupRoutineListId}")
    @Operation(summary = "단체루틴 수정 API", description = "특정 단체루틴의 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateGroupRoutine(@RequestHeader("Authorization") String token,
                                                                @PathVariable Long groupRoutineListId,
                                                                @Valid @RequestBody GroupRoutineRequestDto.Update updateDto) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.updateGroupRoutine(uuid, groupRoutineListId, updateDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.UPDATE_SUCCESS.getMessage()));
    }


    @DeleteMapping("/{groupRoutineListId}")
    @Operation(summary = "단체루틴 삭제 API", description = "특정 단체루틴을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteGroupRoutine(@RequestHeader("Authorization") String token,
                                                                @PathVariable Long groupRoutineListId) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.deleteGroupRoutine(uuid, groupRoutineListId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.DELETE_SUCCESS.getMessage()));
    }


    @PostMapping("/{groupRoutineListId}/join")
    @Operation(summary = "단체루틴 가입 API", description = "특정 단체루틴에 가입합니다.")
    public ResponseEntity<ApiResponse<Void>> joinGroupRoutine(@RequestHeader("Authorization") String token,
                                                              @PathVariable Long groupRoutineListId) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.joinGroupRoutine(uuid, groupRoutineListId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.INSERT_SUCCESS.getMessage()));
    }


    @PostMapping("/{groupRoutineListId}/sub-routines")
    @Operation(summary = "단체루틴 상세 생성 API", description = "특정 단체루틴에 상세 루틴들을 생성합니다.")
    public ResponseEntity<ApiResponse<Void>> createGroupSubRoutines(@RequestHeader("Authorization") String token,
                                                                    @PathVariable Long groupRoutineListId,
                                                                    @Valid @RequestBody SubRoutineRequestDto.Create createDetailDto) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.createGroupSubRoutines(uuid, groupRoutineListId, createDetailDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.INSERT_SUCCESS.getMessage()));
    }


    @PutMapping("/{groupRoutineListId}/sub-routines")
    @Operation(summary = "단체루틴 상세 수정 API", description = "특정 단체루틴의 상세 루틴들을 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateGroupSubRoutines(@RequestHeader("Authorization") String token,
                                                                    @PathVariable Long groupRoutineListId,
                                                                    @Valid @RequestBody SubRoutineRequestDto.Update updateDetailDto) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.updateGroupSubRoutines(uuid, groupRoutineListId, updateDetailDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.UPDATE_SUCCESS.getMessage()));
    }


    @DeleteMapping("/{groupRoutineListId}/sub-routines/{routineId}")
    @Operation(summary = "단체루틴 상세 삭제 API", description = "특정 단체루틴의 특정 상세 루틴을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteGroupSubRoutine(@RequestHeader("Authorization") String token,
                                                                   @PathVariable Long groupRoutineListId,
                                                                   @PathVariable Long routineId) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.deleteGroupSubRoutines(uuid, groupRoutineListId, routineId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.DELETE_SUCCESS.getMessage()));
    }


    @PatchMapping("/{groupRoutineListId}/status/{routineId}")
    @Operation(summary = "단체루틴 상세루틴 성공/실패 처리 API", description = "특정 단체루틴의 상세 루틴의 성공/실패 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> updateGroupRoutineStatus(@RequestHeader("Authorization") String token,
                                                                      @PathVariable Long groupRoutineListId,
                                                                      @PathVariable Long routineId,
                                                                      @Valid @RequestBody SubRoutineRequestDto.StatusUpdate statusDto) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.updateGroupRoutineStatus(uuid, groupRoutineListId, routineId, statusDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.UPDATE_SUCCESS.getMessage()));
    }


    @GetMapping("/{groupRoutineListId}/guestbooks")
    @Operation(summary = "방명록 조회 API", description = "특정 단체루틴의 방명록을 페이지네이션으로 조회합니다.")
    public ResponseEntity<ApiResponse<GuestbookResponseDto.GuestbookList>> getGroupGuestbooks(@RequestHeader("Authorization") String token,
                                                                                              @PathVariable Long groupRoutineListId,
                                                                                              Pageable pageable) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        GuestbookResponseDto.GuestbookList response = groupRoutineService.getGroupGuestbooks(uuid, groupRoutineListId, pageable);
        if (response.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.noContent());
        }
        return ResponseEntity.ok(ApiResponse.onSuccess(response, SuccessStatus.SELECT_SUCCESS.getMessage()));
    }


    @PostMapping("/{groupRoutineListId}/guestbooks")
    @Operation(summary = "방명록 작성 API", description = "특정 단체루틴에 방명록을 작성합니다.")
    public ResponseEntity<ApiResponse<GuestbookResponseDto.GuestbookInfo>> createGroupGuestbook(@RequestHeader("Authorization") String token,
                                                                                                @PathVariable Long groupRoutineListId,
                                                                                                @Valid @RequestBody GuestbookRequestDto.Create guestbookDto) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        GuestbookResponseDto.GuestbookInfo response = groupRoutineService.createGroupGuestbook(uuid, groupRoutineListId, guestbookDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(response, SuccessStatus.INSERT_SUCCESS.getMessage()));
    }


    @DeleteMapping("/{groupRoutineListId}/guestbooks/{guestbookId}")
    @Operation(summary = "방명록 삭제 API", description = "특정 단체루틴의 특정 방명록을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteGroupGuestbook(@RequestHeader("Authorization") String token,
                                                                  @PathVariable Long groupRoutineListId,
                                                                  @PathVariable Long guestbookId) {
        UUID uuid = jwtTokenProvider.getUserId(token.substring(7));
        groupRoutineService.deleteGroupGuestbook(uuid, groupRoutineListId, guestbookId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null, SuccessStatus.DELETE_SUCCESS.getMessage()));
    }
}