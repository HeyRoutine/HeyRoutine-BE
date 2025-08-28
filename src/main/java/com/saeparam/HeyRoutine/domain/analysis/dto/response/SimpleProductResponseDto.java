package com.saeparam.HeyRoutine.domain.analysis.dto.response; // 실제 DTO 패키지 경로에 맞게 수정

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimpleProductResponseDto {
    private String bankName;
    private String accountTypeName;
    private String accountDescription; // description 오타 수정 제안: accountDescription
    private double interestRate; // interestRate 오타 수정 제안

    // ProductRecommendResponseDto.ResultDto 객체를 이 DTO로 변환하는 정적 메소드
    public static SimpleProductResponseDto from(ProductRecommendResponseDto.ResultDto resultDto) {
        return SimpleProductResponseDto.builder()
                .bankName(resultDto.getBankName())
                .accountTypeName(resultDto.getAccountTypeName())
                .accountDescription(resultDto.getAccountDscription())
                .interestRate(resultDto.getInteresRate())
                .build();
    }
}