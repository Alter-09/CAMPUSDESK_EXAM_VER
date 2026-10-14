package com.roles.usermanagement.modules.comment.service;

import com.roles.usermanagement.modules.comment.dto.CommentCreateRequest;
import com.roles.usermanagement.modules.comment.dto.CommentResponse;
import com.roles.usermanagement.modules.comment.entity.Comment;
import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import com.roles.usermanagement.modules.comment.repository.CommentRepository;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import com.roles.usermanagement.persistance.entity.UserEntity;
import com.roles.usermanagement.persistance.entity.UserRoleEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Applies the access and persistence rules for the comment system.
 */
@Service
public class CommentService {

    private final CommentRepository comments;
    private final UserCrudRepository users;

    @PersistenceContext
    private EntityManager entityManager;

    public CommentService(CommentRepository comments, UserCrudRepository users) {
        this.comments = comments;
        this.users = users;
    }

    /**
     * Adds a comment to an active ticket after verifying its owner or assigned technician.
     */
    @Transactional
    public CommentResponse addComment(Long ticketId, CommentCreateRequest request, String username) {
        UserEntity author = findUser(username);
        Ticket ticket = findTicket(ticketId);
        String role = authorizedRole(ticket, author);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw badRequest("No se pueden agregar comentarios a una incidencia cerrada");
        }
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw badRequest("El contenido del comentario no puede estar vacío");
        }

        CommentVisibility visibility = request.visibility() == null
                ? CommentVisibility.PUBLIC
                : request.visibility();
        if (visibility == CommentVisibility.INTERNAL && !"ADMIN".equals(role) && !"TECHNICIAN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo ADMIN o el técnico asignado pueden crear comentarios internos");
        }

        Comment comment = Comment.builder()
                .content(request.content().trim())
                .ticket(ticket)
                .author(author)
                .visibility(visibility)
                .build();
        return toResponse(comments.save(comment));
    }

    /**
     * Lists comments in chronological order and hides internal notes from requesters.
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForTicket(Long ticketId, String username) {
        UserEntity user = findUser(username);
        Ticket ticket = findTicket(ticketId);
        String role = authorizedRole(ticket, user);

        return comments.findByTicket_IdOrderByCreatedAtAsc(ticketId).stream()
                .filter(comment -> comment.getVisibility() != CommentVisibility.INTERNAL
                        || "ADMIN".equals(role) || "TECHNICIAN".equals(role))
                .map(this::toResponse)
                .toList();
    }

    private Ticket findTicket(Long ticketId) {
        Ticket ticket = entityManager.find(Ticket.class, ticketId);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La incidencia no existe");
        }
        return ticket;
    }

    private UserEntity findUser(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario autenticado");
        }
        return users.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));
    }

    private String authorizedRole(Ticket ticket, UserEntity user) {
        if (hasRole(user, "ADMIN")) {
            return "ADMIN";
        }
        if (hasRole(user, "TECHNICIAN")) {
            if (ticket.getTechnician() == null
                    || !ticket.getTechnician().getUsername().equals(user.getUsername())) {
                throw forbidden("Solo el técnico asignado puede acceder a esta incidencia");
            }
            return "TECHNICIAN";
        }
        if (hasRole(user, "USER")) {
            if (!ticket.getApplicant().getUsername().equals(user.getUsername())) {
                throw forbidden("Solo el solicitante puede acceder a esta incidencia");
            }
            return "USER";
        }
        throw forbidden("El rol del usuario no está autorizado para acceder a comentarios");
    }

    private boolean hasRole(UserEntity user, String expectedRole) {
        return user.getRoles() != null && user.getRoles().stream()
                .map(UserRoleEntity::getRole)
                .anyMatch(expectedRole::equals);
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getVisibility(),
                comment.getCreatedAt()
        );
    }

    private ResponseStatusException badRequest(String message) {
        String reason = (message == null || message.isBlank()) ? "Solicitud incorrecta" : message;
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
