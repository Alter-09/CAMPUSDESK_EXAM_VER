package com.roles.usermanagement.modules.comment.dto;

import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import jakarta.validation.constraints.NotBlank;

/**
 * Content and optional visibility of a new comment.
 */
public record CommentCreateRequest(
        @NotBlank(message = "El contenido del comentario es obligatorio")
        String content,
        CommentVisibility visibility,
        // [Nueva Funcionalidad: T-03 - Indicador de nota interna privada] - Determina si el comentario es privado para técnicos - Conecta con: Comment
        Boolean isInternal
) {
}
