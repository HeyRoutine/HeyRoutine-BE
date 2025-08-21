package com.saeparam.HeyRoutine.domain.routine.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saeparam.HeyRoutine.domain.routine.enums.RoutineType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * <h2>GroupRoutineResponseDto</h2>
 * <p>
 * 단체 루틴(Group Routine) 관련 API의 <b>응답(Response)</b> 데이터 전송을 담당하는 DTO 클래스들을 포함합니다.<br>
 * 각 기능에 최적화된 응답 클래스를 내부 정적 클래스로 정의하여 응집도를 높였습니다.
 * </p>
 *
 * <pre>
 * ┌───────────────────────────────┐
 * │ GroupRoutineResponseDto       │
 * ├───────────────────────────────┤
 * │ - ListResponse                │──► 단체 루틴 목록 조회 응답
 * │ - DetailResponse              │──► 단체 루틴 상세 조회 응답
 * │ - GroupRoutineInfo            │──► (공용) 단체 루틴 기본 정보
 * │ - RoutineInfo                 │──► (공용) 상세 루틴 정보
 * │ - GroupRoutineMemberInfo      │──► (공용) 단체 루틴 멤버 정보
 * └───────────────────────────────┘
 * </pre>
 */
public class GroupRoutineResponseDto {

    /**
     * <h3>[Response] 단체 루틴 목록 조회</h3>
     * <p>GET /api/v1/routines/groups</p>
     */
    @Getter
    @Builder
    @Schema(description = "단체 루틴 목록 응답 DTO")
    public static class ListResponse {
        @Schema(description = "단체 루틴 정보 리스트")
        private List<GroupRoutineInfo> items;
    }

    /**
     * <h3>[Response] 단체 루틴 상세 조회</h3>
     * <p>GET /api/v1/routines/groups/{groupRoutineListId}</p>
     */
    @Getter
    @Builder
    @Schema(description = "단체 루틴 상세 정보 응답 DTO")
    public static class DetailResponse {
        @Schema(description = "현재 사용자가 방장인지 여부", example = "false")
        private boolean isAdmin;

        @Schema(description = "단체 루틴 기본 정보")
        private GroupRoutineInfo groupRoutineInfo;

        @Schema(description = "상세 루틴 정보 리스트")
        private List<RoutineInfo> routineInfos;

        @Schema(description = "단체 루틴 멤버 정보 (사용자 상태에 따라 내용 변경)")
        private GroupRoutineMemberInfo groupRoutineMemberInfo;
    }

    /**
     * <h4>(공용) 단체 루틴 기본 정보</h4>
     * <p>단체 루틴 목록/상세 조회 시 공통으로 사용되는 기본 정보 DTO 입니다.</p>
     */
    @Getter
    @Builder
    @Schema(description = "단체 루틴 기본 정보 DTO")
    public static class GroupRoutineInfo {
        @Schema(description = "단체 루틴 ID", example = "1")
        private Long id;

        @Schema(description = "루틴 타입 (DAILY: 일상, FINANCE: 소비)", example = "FINANCE")
        private RoutineType routineType;

        @Schema(description = "단체 루틴 타이틀", example = "티끌모아 태산")
        private String title;

        @Schema(description = "단체 루틴 설명", example = "주 1회 가게부 작성, 뉴스 스크랩 관련 루틴")
        private String description;

        @Schema(description = "루틴 시작 시간 (HH:mm)", example = "20:00")
        private String startTime;

        @Schema(description = "루틴 종료 시간 (HH:mm)", example = "21:00")
        private String endTime;

        @Schema(description = "루틴 개수", example = "5")
        private int routineNums;

        @Schema(description = "참여중인 인원 수", example = "52")
        private int peopleNums;

        @Schema(description = "루틴 수행 요일 리스트", example = "[\"수\", \"일\"]")
        private List<String> dayOfWeek;

        @Schema(description = "현재 사용자의 참여 여부", example = "false")
        private boolean isJoined;
    }

    /**
     * <h4>(공용) 상세 루틴 정보</h4>
     * <p>단체 루틴 상세 조회 시 사용되는 개별 루틴 정보 DTO 입니다.</p>
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JSON 변환 시 제외
    @Schema(description = "상세 루틴 정보 DTO")
    public static class RoutineInfo {
        @Schema(description = "상세 루틴 ID", example = "1")
        private Long id;

        @Schema(description = "이모지 ID", example = "1")
        private Long emojiId;

        @Schema(description = "루틴 이름", example = "커피 내리기")
        private String name;

        @Schema(description = "루틴 소요 시간(분)", example = "3")
        private int time;

        @Schema(description = "[참여자/방장] 루틴 완료 여부", example = "true")
        private Boolean isCompleted;
    }

    /**
     * <h4>(공용) 단체 루틴 멤버 정보</h4>
     * <p>단체 루틴 상세 조회 시 사용자의 참여 상태(미참여/참여/방장)에 따라 다른 정보를 제공하는 DTO 입니다.</p>
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "단체 루틴 멤버 정보 DTO")
    public static class GroupRoutineMemberInfo {
        /** [미참여자용] 참여자 프로필 이미지 URL 리스트 (최대 8개) */
        @Schema(description = "[미참여자] 참여자 프로필 이미지 URL 리스트 (최대 8개)", example = "[\"url1\", \"url2\"]")
        private List<String> profileImageUrl;

        /** [참여자/방장용] 루틴 성공 인원 수 */
        @Schema(description = "[참여자/방장] 루틴 성공 인원 수", example = "12")
        private Integer successPeopleNums;

        /** [참여자/방장용] 루틴 성공 인원의 프로필 이미지 URL 리스트 (최대 8개) */
        @Schema(description = "[참여자/방장] 성공 인원 프로필 이미지 URL 리스트 (최대 8개)", example = "[\"url3\", \"url4\"]")
        private List<String> successPeopleProfileImageUrl;

        /** [참여자/방장용] 루틴 실패 인원 수 */
        @Schema(description = "[참여자/방장] 루틴 실패 인원 수", example = "40")
        private Integer failedPeopleNums;

        /** [참여자/방장용] 루틴 실패 인원의 프로필 이미지 URL 리스트 (최대 8개) */
        @Schema(description = "[참여자/방장] 실패 인원 프로필 이미지 URL 리스트 (최대 8개)", example = "[\"url5\", \"url6\"]")
        private List<String> failedPeopleProfileImageUrl;
    }
}