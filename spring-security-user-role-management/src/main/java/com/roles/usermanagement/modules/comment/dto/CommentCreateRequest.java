package com.roles.usermanagement.modules.comment.dto;

import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import jakarta.validation.constraints.NotBlank;

/**
 * Contenido y visibilidad opcional de un nuevo comentario.
 */
public record CommentCreateRequest(
        @NotBlank(message = "El contenido del comentario es obligatorio")
        String content,
        CommentVisibility visibility
) {
}
