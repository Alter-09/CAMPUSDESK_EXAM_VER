package com.roles.usermanagement.modules.report.controller;

import com.roles.usermanagement.modules.report.service.ReportService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// [Nueva Funcionalidad: U-19/A-11 - Controlador REST de reportes y exportación] - Endpoint para exportar listados de tickets a CSV - Conecta con: ReportService
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Generación y exportación de reportes de tickets")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ReportController {

    private final ReportService reportService;

    @GetMapping(value = "/my-tickets/export", produces = "text/csv")
    @Operation(summary = "Exportar tickets propios o asignados a archivo CSV descargable (MOD-40, U-19, A-11)")
    public ResponseEntity<String> exportMyTickets(Authentication auth) {
        String csv = reportService.exportUserTicketsCsv(auth.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mis_tickets.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
