package com.roles.usermanagement.modules.comment.dto;

import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import java.time.LocalDateTime;

/**
 * Public representation of a ticket comment.
 */
public record CommentResponse(
        Long id,
        Long ticketId,
        String content,
        String authorUsername,
        CommentVisibility visibility,
        LocalDateTime createdAt,
        // [Nueva Funcionalidad: T-03 - Indicador de nota interna y fecha de edición] - Expone estado interno y edición a la API - Conecta con: CommentMapper
        Boolean isInternal,
        LocalDateTime editedAt
) {
}
