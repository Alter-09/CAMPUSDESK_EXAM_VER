package com.roles.usermanagement.modules.knowledgebase.dto;

import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-08/U-09 - Respuesta de artículo de base de conocimiento] - Expone artículo con votos y vistas para autoservicio - Conecta con: KbArticle
public record KbArticleResponse(
        Long id,
        String title,
        String content,
        Long categoryId,
        String categoryName,
        String authorUsername,
        Boolean isPublished,
        Integer viewsCount,
        Integer helpfulVotes,
        Integer unhelpfulVotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
