package com.saeparam.HeyRoutine.domain.shop.dto.request;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopAccountTransferRequestDto {
    private String account;
    private Long price;
}
