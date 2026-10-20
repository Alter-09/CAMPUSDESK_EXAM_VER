package com.roles.usermanagement.modules.tag.controller;

import com.roles.usermanagement.modules.tag.dto.TagRequest;
import com.roles.usermanagement.modules.tag.dto.TagResponse;
import com.roles.usermanagement.modules.tag.service.TicketTagService;
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
public class TagController {

    private final TicketTagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAll() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(request));
    }

    @PostMapping("/ticket/{ticketId}/assign/{tagId}")
    public ResponseEntity<Void> assignToTicket(@PathVariable Long ticketId, @PathVariable Long tagId) {
        tagService.assignTagToTicket(ticketId, tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<TagResponse>> getTagsForTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(tagService.getTagsForTicket(ticketId));
    }
}
