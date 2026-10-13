package com.roles.usermanagement.modules.dashboard.controller;

import com.roles.usermanagement.modules.dashboard.dto.DashboardMetricsResponse;
import com.roles.usermanagement.modules.dashboard.mapper.DashboardMapper;
import com.roles.usermanagement.modules.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapta las solicitudes HTTP autenticadas y las respuestas del caso de uso
 * dashboard a los tipos del contrato de la API.
 */
@RestController
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardMapper dashboardMapper;

    public DashboardController(DashboardService dashboardService, DashboardMapper dashboardMapper) {
        this.dashboardService = dashboardService;
        this.dashboardMapper = dashboardMapper;
    }

    /**
     * Devuelve estadísticas conforme al alcance del usuario autenticado (RT-03 y RF-07).
     */
    @GetMapping({"/api/dashboard/stats", "/api/reports/summary"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardMetricsResponse> getDashboardStats(
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(dashboardMapper.toResponse(dashboardService.getMetricsForUser(username)));
    }
}
