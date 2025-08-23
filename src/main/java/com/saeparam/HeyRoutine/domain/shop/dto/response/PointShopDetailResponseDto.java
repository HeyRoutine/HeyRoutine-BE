package com.saeparam.HeyRoutine.domain.shop.dto.response;

import com.saeparam.HeyRoutine.domain.shop.entity.PointShop;
import com.saeparam.HeyRoutine.domain.shop.enums.PointShopCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointShopDetailResponseDto {

    private String brand;
    private String productName;
    private Long price;
    private Long stock;
    private PointShopCategory category;
    private String imageUrl;

    // PointShop 엔티티를 PointShopDetailResponseDto로 변환하는 정적 팩토리 메서드
    public static PointShopDetailResponseDto toDto(PointShop pointShop) {
        return PointShopDetailResponseDto.builder()
                .brand(pointShop.getBrand())
                .productName(pointShop.getProductName())
                .price(pointShop.getPrice())
                .stock(pointShop.getStock())
                .category(pointShop.getCategory())
                .imageUrl(pointShop.getImageUrl())
                .build();
    }
}
