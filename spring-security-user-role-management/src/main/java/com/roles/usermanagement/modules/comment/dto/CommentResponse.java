package com.roles.usermanagement.modules.comment.dto;

import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import java.time.LocalDateTime;

/**
 * Representación pública de un comentario de incidencia.
 */
public record CommentResponse(
        Long id,
        Long ticketId,
        String content,
        String authorUsername,
        CommentVisibility visibility,
        LocalDateTime createdAt
) {
}
