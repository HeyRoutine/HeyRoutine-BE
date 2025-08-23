package com.saeparam.HeyRoutine.domain.routine.entity;


import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 루틴에 사용되는 이모지 엔티티
 * <p>카테고리별 이모지 조회를 지원하기 위해 {@link Category} 필드를 포함합니다.</p>
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emoji {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emoji_id", updatable = false, unique = true, nullable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 255)
    private Category category;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String emojiUrl;
}
