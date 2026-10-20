package com.roles.usermanagement.modules.knowledgebase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// [Nueva Funcionalidad: A-03/T-12 - Petición de artículo de base de conocimiento] - Transporta título, contenido y categoría de artículo - Conecta con: KbArticle
public record KbArticleRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
        String title,

        @NotBlank(message = "El contenido es obligatorio")
        String content,

        Long categoryId,
        Boolean isPublished
) {
}
