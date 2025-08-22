package com.saeparam.HeyRoutine.domain.routine.controller;

import com.saeparam.HeyRoutine.domain.routine.service.EmojiService;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/emoji")
public class EmojiController {
    private final EmojiService emojiService;


    /**
     * 이모지 전제반환
     */
    @GetMapping("/list")
    @Operation(summary = "모든 이모지 리스트 전체조회 API", description = "모든 이모지를 반환합니다 페이지네이션 10개씩 반환.")
    public ResponseEntity<?> showMyRoutineList(@PageableDefault(size = 10) Pageable pageable){
        return ResponseEntity.ok().body(ApiResponse.onSuccess(emojiService.showEmoji(pageable)));
    }

    }
