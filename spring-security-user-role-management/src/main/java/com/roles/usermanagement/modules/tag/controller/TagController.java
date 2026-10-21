package com.roles.usermanagement.modules.tag.controller;

import com.roles.usermanagement.modules.tag.dto.TagRequest;
import com.roles.usermanagement.modules.tag.dto.TagResponse;
import com.roles.usermanagement.modules.tag.service.TicketTagService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [Nueva Funcionalidad: U-22/A-23 - Controlador REST de etiquetas] - Endpoints para catálogo de tags y asignación a tickets - Conecta con: TicketTagService
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Catálogo de etiquetas y clasificación de incidencias")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TagController {

    private final TicketTagService tagService;

    @GetMapping
    @Operation(summary = "Listar catálogo global de etiquetas disponibles (MOD-35, A-23)")
    public ResponseEntity<List<TagResponse>> getAll() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Operation(summary = "Crear nueva etiqueta en el catálogo (ADMIN o TECHNICIAN) (MOD-35)")
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(request));
    }

    @PostMapping("/ticket/{ticketId}/assign/{tagId}")
    @Operation(summary = "Asignar etiqueta existente a una incidencia (MOD-35, U-22)")
    public ResponseEntity<Void> assignToTicket(@PathVariable Long ticketId, @PathVariable Long tagId) {
        tagService.assignTagToTicket(ticketId, tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "Listar etiquetas asociadas a un ticket específico (MOD-35)")
    public ResponseEntity<List<TagResponse>> getTagsForTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(tagService.getTagsForTicket(ticketId));
    }
}
