package com.roles.usermanagement.modules.comment.repository;

import com.roles.usermanagement.modules.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence for comments with chronological retrieval by ticket.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Lists the oldest comments for the ticket first.
     */
    List<Comment> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);
}
