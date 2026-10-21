package com.roles.usermanagement.modules.announcement.controller;

import com.roles.usermanagement.modules.announcement.dto.AnnouncementRequest;
import com.roles.usermanagement.modules.announcement.dto.AnnouncementResponse;
import com.roles.usermanagement.modules.announcement.service.AnnouncementService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [Nueva Funcionalidad: U-16/A-05 - Controlador REST de anuncios del sistema] - Endpoints para consultar avisos vigentes y crear avisos institucionales - Conecta con: AnnouncementService
@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
@Tag(name = "Announcements", description = "Avisos institucionales, mantenimientos y novedades del campus")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/active")
    @Operation(summary = "Consultar anuncios y avisos activos del sistema (Público) (MOD-30, U-16)")
    public ResponseEntity<List<AnnouncementResponse>> getActive() {
        return ResponseEntity.ok(announcementService.getActiveAnnouncements());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Publicar nuevo aviso institucional o de mantenimiento (ADMIN) (MOD-31, A-05)")
    public ResponseEntity<AnnouncementResponse> create(
            @Valid @RequestBody AnnouncementRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.createAnnouncement(request, auth.getName()));
    }
}
