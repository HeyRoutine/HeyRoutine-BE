package com.saeparam.HeyRoutine.domain.shop.service;


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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointShopService {

    private final PointShopRepository pointShopRepository;
    private final UserRepository userRepository;

    @Transactional
    public String postProduct(PointShopPostRequestDto pointShopPostRequestDto) {
        pointShopRepository.save(PointShopPostRequestDto.toEntity(pointShopPostRequestDto));
        return "상품이 등록되었습니다.";
    }

    @Transactional(readOnly = true)
    public String mypoint(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        return user.getPoint().toString();
    }

    @Transactional(readOnly = true)
    public List<PointShopListResponseDto> shopList(Pageable pageable) {
        Page<PointShop> productList = pointShopRepository.findAll(pageable);
        return productList.map(PointShopListResponseDto::toDto).stream().toList();
    }

    @Transactional(readOnly = true)
    public List<PointShopListResponseDto> shopCategoryList(Pageable pageable, PointShopCategory category) {
        Page<PointShop> product = pointShopRepository.findByCategory(category,pageable);
        return product.map(PointShopListResponseDto::toDto).stream().toList();

    }
    @Transactional(readOnly = true)
    public PointShopDetailResponseDto getProductDetail(Long productId) {
        PointShop product = pointShopRepository.findById(productId)
                .orElseThrow(() -> new ShopHandler(ErrorStatus.PRODUCT_NOT_FOUND));

        return PointShopDetailResponseDto.toDto(product);
    }


    @DistributedLock(key = "#lockName")
    public String buyProduct(String lockName,String email, Long productId) {
        // 1. 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 상품 정보 조회
        PointShop product = pointShopRepository.findById(productId)
                .orElseThrow(() -> new ShopHandler(ErrorStatus.PRODUCT_NOT_FOUND));

        if (product.getStock() <= 0) {
            throw new ShopHandler(ErrorStatus.STOCK_IS_NULL);
        }
        if(user.getPoint()<product.getPrice()){
            throw new ShopHandler(ErrorStatus.USER_POINT_LACK);
        }
        product.minusStock();
        user.usePoints(product.getPrice());
        userRepository.save(user);
        pointShopRepository.save(product);
        // 기프티콘을 보내줘야할 듯 ?

        return "상품 구매가 완료되었습니다.";
    }



}
