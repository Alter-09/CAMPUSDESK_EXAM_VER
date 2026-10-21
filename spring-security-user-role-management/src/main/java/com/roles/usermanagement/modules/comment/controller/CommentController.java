package com.roles.usermanagement.modules.comment.controller;

import com.roles.usermanagement.modules.comment.dto.CommentCreateRequest;
import com.roles.usermanagement.modules.comment.dto.CommentResponse;
import com.roles.usermanagement.modules.comment.service.CommentService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Protected endpoints for retrieving and adding ticket comments.
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Comments", description = "Gestión de comentarios y notas internas de incidencias")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Creates a comment and returns HTTP 201.
     */
    @PostMapping
    @Operation(summary = "Crear comentario público o nota interna privada en un ticket (MOD-19, T-03)")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable @Positive Long ticketId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(ticketId, request, username));
    }

    /**
     * Retrieves the comments visible to the authenticated user.
     */
    @GetMapping
    @Operation(summary = "Listar comentarios visibles según rol del usuario autenticado (MOD-20)")
    public ResponseEntity<List<CommentResponse>> getCommentsByTicket(
            @PathVariable @Positive Long ticketId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(commentService.getCommentsForTicket(ticketId, username));
    }
}
