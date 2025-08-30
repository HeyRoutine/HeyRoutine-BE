package com.saeparam.HeyRoutine.domain.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON으로 변환 시 제외
public class ExpoPushRequestDto {
    private List<String> to;
    private String sound;
    private String title;
    private String body;
    private String image; // Expo에서는 image 필드를 직접 지원하지 않으며, data 필드를 통해 전달해야 할 수 있습니다. (API 명세 재확인 필요)
}