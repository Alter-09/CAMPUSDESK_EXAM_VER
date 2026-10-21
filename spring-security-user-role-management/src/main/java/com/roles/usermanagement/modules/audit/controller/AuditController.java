package com.roles.usermanagement.modules.audit.controller;

import com.roles.usermanagement.domain.dto.PageResponse;
import com.roles.usermanagement.modules.audit.dto.AuditLogResponse;
import com.roles.usermanagement.modules.audit.service.AuditService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// [Nueva Funcionalidad: A-18 - Controlador REST de auditoría] - Endpoints para consulta paginada del log de auditoría del sistema - Conecta con: AuditService
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('AUDIT_READ') or hasRole('SUPERADMIN')")
@Tag(name = "Audit Logs", description = "Trazabilidad inmutable de eventos y acciones administrativas del sistema")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Consultar registro paginado de trazas de auditoría global (MOD-37, A-18)")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.getAllLogs(page, size));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    @Operation(summary = "Consultar historial de auditoría específico de una entidad por identificador (MOD-37)")
    public ResponseEntity<PageResponse<AuditLogResponse>> getLogsByEntity(
            @PathVariable String entityName,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.getLogsByEntity(entityName, entityId, page, size));
    }
}
