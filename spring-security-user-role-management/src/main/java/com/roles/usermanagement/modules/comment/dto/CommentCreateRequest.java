package com.roles.usermanagement.modules.comment.dto;

import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import jakarta.validation.constraints.NotBlank;

/**
 * Content and optional visibility of a new comment.
 */
public record CommentCreateRequest(
        @NotBlank(message = "El contenido del comentario es obligatorio")
        String content,
        CommentVisibility visibility
) {
}
