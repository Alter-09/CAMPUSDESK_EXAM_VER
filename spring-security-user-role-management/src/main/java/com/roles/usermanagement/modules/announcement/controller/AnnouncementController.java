package com.roles.usermanagement.modules.announcement.controller;

import com.roles.usermanagement.modules.announcement.dto.AnnouncementRequest;
import com.roles.usermanagement.modules.announcement.dto.AnnouncementResponse;
import com.roles.usermanagement.modules.announcement.service.AnnouncementService;
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
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/active")
    public ResponseEntity<List<AnnouncementResponse>> getActive() {
        return ResponseEntity.ok(announcementService.getActiveAnnouncements());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnouncementResponse> create(
            @Valid @RequestBody AnnouncementRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.createAnnouncement(request, auth.getName()));
    }
}
