package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.service.GroupRoutineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routines/groups")
public class GroupRoutineController {

    private final GroupRoutineService groupRoutineService;

    @GetMapping
    @Operation(summary = "단체루틴 리스트 조회 API", description = "모든 단체루틴 리스트를 페이지네이션으로 조회합니다.")
    public ResponseEntity<?> getGroupRoutines(@RequestHeader("Authorization") String token,
                                              Pageable pageable) {
        // TODO: Implement service logic
        return null;
    }

    @PostMapping
    @Operation(summary = "단체루틴 생성 API", description = "새로운 단체루틴을 생성합니다.")
    public ResponseEntity<?> createGroupRoutine(@RequestHeader("Authorization") String token,
                                                @RequestBody Object createDto) {
        // TODO: Implement service logic
        return null;
    }

    @GetMapping("/{groupRoutineListId}")
    @Operation(summary = "단체루틴 상세 조회 API", description = "특정 단체루틴의 상세 정보를 조회합니다. 사용자의 참여 여부에 따라 다른 정보가 반환됩니다.")
    public ResponseEntity<?> getGroupRoutineDetail(@RequestHeader("Authorization") String token,
                                                   @PathVariable Long groupRoutineListId) {
        // TODO: Implement service logic
        return null;
    }

    @PutMapping("/{groupRoutineListId}")
    @Operation(summary = "단체루틴 수정 API", description = "특정 단체루틴의 정보를 수정합니다.")
    public ResponseEntity<?> updateGroupRoutine(@RequestHeader("Authorization") String token,
                                                @PathVariable Long groupRoutineListId,
                                                @RequestBody Object updateDto) {
        // TODO: Implement service logic
        return null;
    }

    @DeleteMapping("/{groupRoutineListId}")
    @Operation(summary = "단체루틴 삭제 API", description = "특정 단체루틴을 삭제합니다.")
    public ResponseEntity<?> deleteGroupRoutine(@RequestHeader("Authorization") String token,
                                                @PathVariable Long groupRoutineListId) {
        // TODO: Implement service logic
        return null;
    }

    @PostMapping("/{groupRoutineListId}/join")
    @Operation(summary = "단체루틴 가입 API", description = "특정 단체루틴에 가입합니다.")
    public ResponseEntity<?> joinGroupRoutine(@RequestHeader("Authorization") String token,
                                              @PathVariable Long groupRoutineListId) {
        // TODO: Implement service logic
        return null;
    }

    @PostMapping("/{groupRoutineListId}/sub-routines")
    @Operation(summary = "단체루틴 상세 생성 API", description = "특정 단체루틴에 상세 루틴들을 생성합니다.")
    public ResponseEntity<?> createGroupRoutineDetail(@RequestHeader("Authorization") String token,
                                                      @PathVariable Long groupRoutineListId,
                                                      @RequestBody Object createDetailDto) {
        // TODO: Implement service logic
        return null;
    }

    @PutMapping("/{groupRoutineListId}/sub-routines")
    @Operation(summary = "단체루틴 상세 수정 API", description = "특정 단체루틴의 상세 루틴들을 수정합니다.")
    public ResponseEntity<?> updateGroupRoutineDetail(@RequestHeader("Authorization") String token,
                                                      @PathVariable Long groupRoutineListId,
                                                      @RequestBody Object updateDetailDto) {
        // TODO: Implement service logic
        return null;
    }

    @DeleteMapping("/{groupRoutineListId}/sub-routines/{routineId}")
    @Operation(summary = "단체루틴 상세 삭제 API", description = "특정 단체루틴의 특정 상세 루틴을 삭제합니다.")
    public ResponseEntity<?> deleteGroupRoutineDetail(@RequestHeader("Authorization") String token,
                                                      @PathVariable Long groupRoutineListId,
                                                      @PathVariable Long routineId) {
        // TODO: Implement service logic
        return null;
    }

    @PatchMapping("/{groupRoutineListId}/status/{routineId}")
    @Operation(summary = "단체루틴 상세루틴 성공/실패 처리 API", description = "특정 단체루틴의 상세 루틴의 성공/실패 상태를 변경합니다.")
    public ResponseEntity<?> updateGroupRoutineStatus(@RequestHeader("Authorization") String token,
                                                      @PathVariable Long groupRoutineListId,
                                                      @PathVariable Long routineId,
                                                      @RequestBody Object statusDto) {
        // TODO: Implement service logic
        return null;
    }

    @GetMapping("/{groupRoutineListId}/guestbooks")
    @Operation(summary = "방명록 조회 API", description = "특정 단체루틴의 방명록을 페이지네이션으로 조회합니다.")
    public ResponseEntity<?> getGroupGuestbooks(@RequestHeader("Authorization") String token,
                                                @PathVariable Long groupRoutineListId,
                                                Pageable pageable) {
        // TODO: Implement service logic
        return null;
    }

    @PostMapping("/{groupRoutineListId}/guestbooks")
    @Operation(summary = "방명록 작성 API", description = "특정 단체루틴에 방명록을 작성합니다.")
    public ResponseEntity<?> createGroupGuestbook(@RequestHeader("Authorization") String token,
                                                  @PathVariable Long groupRoutineListId,
                                                  @RequestBody Object guestbookDto) {
        // TODO: Implement service logic
        return null;
    }

    @DeleteMapping("/{groupRoutineListId}/guestbooks/{guestbookId}")
    @Operation(summary = "방명록 삭제 API", description = "특정 단체루틴의 특정 방명록을 삭제합니다.")
    public ResponseEntity<?> deleteGroupGuestbook(@RequestHeader("Authorization") String token,
                                                  @PathVariable Long groupRoutineListId,
                                                  @PathVariable Long guestbookId) {
        // TODO: Implement service logic
        return null;
    }
}