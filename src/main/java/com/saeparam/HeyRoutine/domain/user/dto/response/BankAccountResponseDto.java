//package com.saeparam.HeyRoutine.domain.user.dto.response;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.ToString; // 디버깅 시 객체 내용을 쉽게 확인하기 위해 추가하면 좋습니다.
//
///**
// * API 응답의 최상위 구조를 담는 DTO 클래스입니다.
// * Header와 REC 객체를 포함합니다.
// */
//@Getter
//@NoArgsConstructor
//@ToString // 로그 출력 시 필드 값을 보기 편하게 해줍니다.
//public class BankAccountResponseDto {
//
//    @JsonProperty("Header")
//    private ResponseHeaderDto header;
//
//    @JsonProperty("REC")
//    private RecDto rec;
//}
//
///**
// * 응답 JSON의 Header 부분을 담당하는 DTO 클래스입니다.
// */
//@Getter
//@NoArgsConstructor
//@ToString
//class ResponseHeaderDto {
//    private String responseCode;
//    private String responseMessage;
//    private String apiName;
//    private String transmissionDate;
//    private String transmissionTime;
//    private String institutionCode;
//    private String apiKey;
//    private String apiServiceCode;
//    private String institutionTransactionUniqueNo;
//}
//
///**
// * 응답 JSON의 REC (실제 데이터) 부분을 담당하는 DTO 클래스입니다.
// */
//@Getter
//@NoArgsConstructor
//@ToString
//class RecDto {
//    private String bankCode;
//    private String accountNo;
//    private CurrencyDto currency;
//}
//
///**
// * REC 객체 내부에 중첩된 currency 정보를 담당하는 DTO 클래스입니다.
// */
//@Getter
//@NoArgsConstructor
//@ToString
//class CurrencyDto {
//    private String currency;
//    private String currencyName;
//}
package com.saeparam.HeyRoutine.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * API 응답의 최상위 구조를 담는 DTO 클래스입니다.
 * Header와 REC 객체를 포함합니다.
 */
@Getter
@NoArgsConstructor
@ToString
public class BankAccountResponseDto {

    @JsonProperty("Header")
    private ResponseHeaderDto header;

    @JsonProperty("REC")
    private RecDto rec;

    /**
     * 응답 JSON의 Header 부분을 담당하는 DTO 클래스입니다.
     * 외부에서 접근할 수 있도록 public static으로 변경합니다.
     */
    @Getter
    @NoArgsConstructor
    @ToString
    public static class ResponseHeaderDto {
        private String responseCode;
        private String responseMessage;
        private String apiName;
        private String transmissionDate;
        private String transmissionTime;
        private String institutionCode;
        private String apiKey;
        private String apiServiceCode;
        private String institutionTransactionUniqueNo;
    }

    /**
     * 응답 JSON의 REC (실제 데이터) 부분을 담당하는 DTO 클래스입니다.
     * 외부에서 접근할 수 있도록 public static으로 변경합니다.
     */
    @Getter
    @NoArgsConstructor
    @ToString
    public static class RecDto {
        private String bankCode;
        private String accountNo;
        private CurrencyDto currency;
    }

    /**
     * REC 객체 내부에 중첩된 currency 정보를 담당하는 DTO 클래스입니다.
     * 외부에서 접근할 수 있도록 public static으로 변경합니다.
     */
    @Getter
    @NoArgsConstructor
    @ToString
    public static class CurrencyDto {
        private String currency;
        private String currencyName;
    }
}
