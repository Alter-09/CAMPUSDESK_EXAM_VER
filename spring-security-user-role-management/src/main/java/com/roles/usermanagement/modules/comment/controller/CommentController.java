package com.roles.usermanagement.modules.comment.controller;

import com.roles.usermanagement.modules.comment.dto.CommentCreateRequest;
import com.roles.usermanagement.modules.comment.dto.CommentResponse;
import com.roles.usermanagement.modules.comment.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints protegidos para consultar y agregar comentarios de incidencias.
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@PreAuthorize("isAuthenticated()")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Crea un comentario y devuelve HTTP 201.
     */
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable @Positive Long ticketId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(ticketId, request, username));
    }

    /**
     * Recupera los comentarios visibles para el usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByTicket(
            @PathVariable @Positive Long ticketId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(commentService.getCommentsForTicket(ticketId, username));
    }
}
