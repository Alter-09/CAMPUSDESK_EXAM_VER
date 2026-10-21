package com.roles.usermanagement.modules.comment.entity;

import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.persistance.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a comment associated with a ticket, its author, and its visibility
 * scope (RF-05).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_comments", indexes = {
        @Index(name = "idx_comment_ticket_created", columnList = "ticket_id, created_at")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * User who authored the comment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_username", referencedColumnName = "username", nullable = false)
    private UserEntity author;

    /**
     * Ticket to which the comment belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentVisibility visibility;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // [Nueva Funcionalidad: T-03 - Notas internas privadas para técnicos] - Oculta comentarios internos a solicitantes - Conecta con: CommentRepository y CommentService
    @Column(name = "is_internal", nullable = false)
    @Builder.Default
    private Boolean isInternal = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    /** Sets the creation timestamp and the default public visibility. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.visibility == null) {
            this.visibility = CommentVisibility.PUBLIC;
        }
    }

}
