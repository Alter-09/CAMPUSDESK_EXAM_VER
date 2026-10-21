package com.roles.usermanagement.modules.knowledgebase.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-08/U-09/A-03/U-21 - Artículos de base de conocimiento] - Documentación de autoayuda con conteo de votos útiles y vistas - Conecta con: KbCategory y KnowledgeBaseService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "kb_articles", indexes = {
        @Index(name = "idx_kb_category", columnList = "category_id"),
        @Index(name = "idx_kb_published", columnList = "is_published")
})
public class KbArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private KbCategory category;

    @Column(name = "author_username", nullable = false, length = 50)
    private String authorUsername;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = true;

    @Column(name = "views_count", nullable = false)
    @Builder.Default
    private Integer viewsCount = 0;

    @Column(name = "helpful_votes", nullable = false)
    @Builder.Default
    private Integer helpfulVotes = 0;

    @Column(name = "unhelpful_votes", nullable = false)
    @Builder.Default
    private Integer unhelpfulVotes = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isPublished == null) this.isPublished = true;
        if (this.viewsCount == null) this.viewsCount = 0;
        if (this.helpfulVotes == null) this.helpfulVotes = 0;
        if (this.unhelpfulVotes == null) this.unhelpfulVotes = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
