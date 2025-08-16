package com.saeparam.HeyRoutine.domain.shop.service;


import com.saeparam.HeyRoutine.domain.shop.dto.request.PointShopPostRequestDto;
import com.saeparam.HeyRoutine.domain.shop.dto.response.PointShopListResponseDto;
import com.saeparam.HeyRoutine.domain.shop.entity.PointShop;
import com.saeparam.HeyRoutine.domain.shop.repository.PointShopRepository;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointShopService {

    private final PointShopRepository pointShopRepository;
    private final UserRepository userRepository;
    public String postProduct(PointShopPostRequestDto pointShopPostRequestDto) {
        pointShopRepository.save(PointShopPostRequestDto.toEntity(pointShopPostRequestDto));
        return "상품이 등록되었습니다.";
    }

    public String mypoint(String email) {
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new UserHandler(ErrorStatus.USER_NOT_FOUND));
        return user.getPoint().toString();
    }

    public List<PointShopListResponseDto> shopList(Pageable pageable) {
        Page<PointShop> productList=pointShopRepository.findAll(pageable);
        return productList.map(PointShopListResponseDto::toDto).stream().toList();
    }
}
