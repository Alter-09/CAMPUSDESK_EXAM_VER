package com.roles.usermanagement.modules.comment.repository;

import com.roles.usermanagement.modules.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistencia de comentarios con recuperación cronológica por incidencia.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Lista primero los comentarios más antiguos de la incidencia.
     */
    List<Comment> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);
}
