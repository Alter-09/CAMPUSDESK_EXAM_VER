package com.roles.usermanagement.modules.comment.entity;

import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.persistance.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa un comentario asociado a una incidencia, su autor y su ámbito de
 * visibilidad (RF-05).
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
     * Usuario autor del comentario.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_username", referencedColumnName = "username", nullable = false)
    private UserEntity author;

    /**
     * Incidencia a la que pertenece el comentario.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentVisibility visibility;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Asigna la fecha de creación y la visibilidad pública predeterminada. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.visibility == null) {
            this.visibility = CommentVisibility.PUBLIC;
        }
    }

}
