package com.roles.usermanagement.modules.cannedresponse.controller;

import com.roles.usermanagement.modules.cannedresponse.dto.CannedResponseRequest;
import com.roles.usermanagement.modules.cannedresponse.dto.CannedResponseResponse;
import com.roles.usermanagement.modules.cannedresponse.service.CannedResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [Nueva Funcionalidad: T-04/A-19 - Controlador REST de respuestas rápidas] - Endpoints para consultar y crear respuestas predefinidas - Conecta con: CannedResponseService
@RestController
@RequestMapping("/api/v1/canned-responses")
@RequiredArgsConstructor
public class CannedResponseController {

    private final CannedResponseService cannedResponseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    public ResponseEntity<List<CannedResponseResponse>> getAvailable(Authentication auth) {
        return ResponseEntity.ok(cannedResponseService.getAvailableResponses(auth.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    public ResponseEntity<CannedResponseResponse> create(
            @Valid @RequestBody CannedResponseRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cannedResponseService.createResponse(request, auth.getName()));
    }
}
