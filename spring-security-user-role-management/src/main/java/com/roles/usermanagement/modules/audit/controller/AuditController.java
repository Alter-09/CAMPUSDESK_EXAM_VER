package com.roles.usermanagement.modules.audit.controller;

import com.roles.usermanagement.domain.dto.PageResponse;
import com.roles.usermanagement.modules.audit.dto.AuditLogResponse;
import com.roles.usermanagement.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// [Nueva Funcionalidad: A-18 - Controlador REST de auditoría] - Endpoints para consulta paginada del log de auditoría del sistema - Conecta con: AuditService
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<PageResponse<AuditLogResponse>> getLogsByEntity(
            @PathVariable String entityName,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.getLogsByEntity(entityName, entityId, page, size));
    }
}
