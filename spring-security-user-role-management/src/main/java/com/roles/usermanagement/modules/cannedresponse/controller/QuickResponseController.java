package com.roles.usermanagement.modules.cannedresponse.controller;

import com.roles.usermanagement.modules.cannedresponse.dto.QuickResponseRequest;
import com.roles.usermanagement.modules.cannedresponse.dto.QuickResponseResponse;
import com.roles.usermanagement.modules.cannedresponse.service.QuickResponseService;
import com.roles.usermanagement.modules.comment.dto.CommentResponse;
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

/**
 * Endpoints para gestión de plantillas de solución rápida y aplicación a tickets (Ejercicio 5 Examen).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Plantillas Técnicas", description = "CRUD de plantillas QuickResponse e inyección en comentarios de incidencias")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class QuickResponseController {

    private final QuickResponseService quickResponseService;

    @PostMapping("/api/tecnicos/plantillas")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Crear plantilla de solución rápida para el técnico autenticado")
    public ResponseEntity<QuickResponseResponse> createPlantilla(
            @Valid @RequestBody QuickResponseRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quickResponseService.createPlantilla(request, auth.getName()));
    }

    @GetMapping("/api/tecnicos/plantillas")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Listar plantillas exclusivas del técnico autenticado")
    public ResponseEntity<List<QuickResponseResponse>> getMisPlantillas(Authentication auth) {
        return ResponseEntity.ok(quickResponseService.getMisPlantillas(auth.getName()));
    }

    @PostMapping("/api/tickets/{id}/aplicar-plantilla/{plantillaId}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Insertar plantilla como comentario formal en ticket asignado en EN_PROCESO")
    public ResponseEntity<CommentResponse> aplicarPlantilla(
            @PathVariable Long id,
            @PathVariable Long plantillaId,
            Authentication auth) {
        return ResponseEntity.ok(quickResponseService.aplicarPlantilla(id, plantillaId, auth.getName()));
    }
}
