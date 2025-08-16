package com.saeparam.HeyRoutine.domain.shop.controller;

import com.saeparam.HeyRoutine.domain.shop.dto.request.PointShopPostRequestDto;
import com.saeparam.HeyRoutine.domain.shop.dto.response.PointShopListResponseDto;
import com.saeparam.HeyRoutine.domain.shop.service.PointShopService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop")
public class PointShopController {

    private final PointShopService pointShopService;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/my-point")
    @Operation(summary = "내 포인트 조회 API", description = "내 포인트를 조회합니다.")
    public ResponseEntity<?> myPoint(@RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmail(token.substring(7));

        String result=pointShopService.mypoint(email);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }


    @PostMapping()
    @Operation(summary = "물건 등록하기 API", description = "포인트샵에 물건을 등록합니다")
    public ResponseEntity<?> postProduct(@RequestBody PointShopPostRequestDto pointShopPostRequestDto) {

        String result=pointShopService.postProduct(pointShopPostRequestDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    @GetMapping("/list")
    @Operation(summary = "물건 전체보기 API", description = "물건 전체를 조회합니다.")
    public ResponseEntity<?> shopList( @PageableDefault(size = 10, sort = "stock") Pageable pageable) {

        List<PointShopListResponseDto> result=pointShopService.shopList(pageable);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }


}
