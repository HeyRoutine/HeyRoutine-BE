package com.saeparam.HeyRoutine.domain.routine.entity;

import com.saeparam.HeyRoutine.domain.routine.enums.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id", updatable = false, unique = true, nullable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 255)
    private Category category;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "content", nullable = false, length = 255)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emoji_id", nullable = false)
    private Emoji emoji;
}
