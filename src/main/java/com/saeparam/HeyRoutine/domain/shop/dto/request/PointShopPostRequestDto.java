package com.saeparam.HeyRoutine.domain.shop.dto.request;

import com.saeparam.HeyRoutine.domain.shop.entity.PointShop;
import com.saeparam.HeyRoutine.domain.shop.enums.PointShopCategory;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointShopPostRequestDto {

  private String brand;
  private String productName;
  private Long price;
  private Long stock;
  private PointShopCategory pointShopCategory;
  private String imageUrl;

  public static PointShop toEntity(PointShopPostRequestDto pointShopPostRequestDto){
    return PointShop.builder()
            .brand(pointShopPostRequestDto.getBrand())
            .category(pointShopPostRequestDto.getPointShopCategory())
            .price(pointShopPostRequestDto.getPrice())
            .productName(pointShopPostRequestDto.productName)
            .stock(pointShopPostRequestDto.getStock())
            .imageUrl(pointShopPostRequestDto.imageUrl)
            .build();
  }
}
