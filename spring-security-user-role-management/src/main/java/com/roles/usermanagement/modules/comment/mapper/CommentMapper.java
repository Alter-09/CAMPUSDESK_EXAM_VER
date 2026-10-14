package com.roles.usermanagement.modules.comment.mapper;

import com.roles.usermanagement.modules.comment.dto.CommentResponse;
import com.roles.usermanagement.modules.comment.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentResponse toResponse(Comment com){
        return new CommentResponse(
                com.getId(),
                com.getTicket().getId(),
                com.getAuthor().getUsername(),
                com.getContent(),
                com.getVisibility(),
                com.getCreatedAt()
        );
    }
}
