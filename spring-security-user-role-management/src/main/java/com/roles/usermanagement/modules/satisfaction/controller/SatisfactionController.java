package com.roles.usermanagement.modules.satisfaction.controller;

import com.roles.usermanagement.modules.satisfaction.dto.CsatRequest;
import com.roles.usermanagement.modules.satisfaction.dto.CsatResponse;
import com.roles.usermanagement.modules.satisfaction.service.SatisfactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// [Nueva Funcionalidad: U-06/A-21 - Controlador REST de satisfacción CSAT] - Endpoints para envío de encuestas y visualización de promedios - Conecta con: SatisfactionService
@RestController
@RequestMapping("/api/v1/satisfaction")
@RequiredArgsConstructor
public class SatisfactionController {

    private final SatisfactionService satisfactionService;

    @PostMapping("/tickets/{ticketId}")
    public ResponseEntity<CsatResponse> submitCsat(
            @PathVariable Long ticketId,
            @Valid @RequestBody CsatRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(satisfactionService.submitCsat(ticketId, request, auth.getName()));
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<CsatResponse> getByTicketId(@PathVariable Long ticketId) {
        CsatResponse response = satisfactionService.getByTicketId(ticketId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    @GetMapping("/average")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Map<String, Double>> getAverage() {
        return ResponseEntity.ok(Map.of("averageScore", satisfactionService.getAverageScore()));
    }
}
