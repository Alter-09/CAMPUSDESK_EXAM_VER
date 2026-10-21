package com.roles.usermanagement.modules.comment.mapper;

import com.roles.usermanagement.modules.comment.dto.CommentResponse;
import com.roles.usermanagement.modules.comment.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    // [Nueva Funcionalidad: T-03 - Mapeo de campos ampliados de comentario] - Mapea isInternal y editedAt al DTO de respuesta - Conecta con: Comment y CommentResponse
    public CommentResponse toResponse(Comment com){
        return new CommentResponse(
                com.getId(),
                com.getTicket().getId(),
                com.getContent(),
                com.getAuthor().getUsername(),
                com.getVisibility(),
                com.getCreatedAt(),
                com.getIsInternal(),
                com.getEditedAt()
        );
    }
}
