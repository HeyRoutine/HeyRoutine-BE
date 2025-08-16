package com.saeparam.HeyRoutine.domain.shop.dto.response;

import com.saeparam.HeyRoutine.domain.shop.entity.PointShop;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointShopListResponseDto {


  private Long id;
  private String productName;
  private String brand;
  private Long price;
  private Long stock;
  private String imageUrl;

  public static PointShopListResponseDto toDto(PointShop pointShop){
    return PointShopListResponseDto.builder()
            .id(pointShop.getId())
            .brand(pointShop.getBrand())
            .price(pointShop.getPrice())
            .productName(pointShop.getProductName())
            .stock(pointShop.getStock())
            .imageUrl(pointShop.getImageUrl())
            .build();
  }
}
