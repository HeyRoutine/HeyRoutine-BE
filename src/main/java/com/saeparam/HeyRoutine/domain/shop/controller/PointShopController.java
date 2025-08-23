package com.saeparam.HeyRoutine.domain.shop.controller;

import com.saeparam.HeyRoutine.domain.shop.dto.request.PointShopPostRequestDto;
import com.saeparam.HeyRoutine.domain.shop.dto.response.PointShopDetailResponseDto;
import com.saeparam.HeyRoutine.domain.shop.dto.response.PointShopListResponseDto;
import com.saeparam.HeyRoutine.domain.shop.enums.PointShopCategory;
import com.saeparam.HeyRoutine.domain.shop.service.PointShopService;
import com.saeparam.HeyRoutine.global.security.jwt.JwtTokenProvider;
import com.saeparam.HeyRoutine.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop")
public class PointShopController {

    private final PointShopService pointShopService;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/my-point")
    @Operation(summary = "내 포인트 조회 API", description = "내 포인트를 조회합니다.")
    public ResponseEntity<?> myPoint(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));

        String result=pointShopService.mypoint(userId);
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

        return ResponseEntity.ok().body(ApiResponse.onSuccess(pointShopService.shopList(pageable)));
    }
    @GetMapping("/list/{category}")
    @Operation(summary = "물건 카테고리별 전체보기 API", description = "물건 카테고리별 조회합니다.")
    public ResponseEntity<?> shopCategoryList( @PageableDefault(size = 10, sort = "stock") Pageable pageable,@PathVariable PointShopCategory category) {

        return ResponseEntity.ok().body(ApiResponse.onSuccess(pointShopService.shopCategoryList(pageable,category)));
    }


    @GetMapping("/{id}")
    @Operation(summary = "물건 상세보기 API", description = "특정 물건의 상세 정보를 조회합니다.")
    public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
        PointShopDetailResponseDto result = pointShopService.getProductDetail(id);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    @PostMapping("/buy/{id}")
    @Operation(summary = "물건 결제하기 API", description = "물건을 결제합니다.")
    public ResponseEntity<?> buyProduct(@RequestHeader("Authorization") String token,@PathVariable Long id) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));

        String result=pointShopService.buyProduct("shop"+id,userId,id);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

}
