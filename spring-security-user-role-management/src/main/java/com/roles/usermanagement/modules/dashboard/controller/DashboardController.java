package com.roles.usermanagement.modules.dashboard.controller;

import com.roles.usermanagement.modules.dashboard.dto.DashboardMetricsResponse;
import com.roles.usermanagement.modules.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint autenticado para consultar las métricas del panel de indicadores.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Devuelve estadísticas conforme al alcance del usuario autenticado.
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardMetricsResponse> getDashboardStats(
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(dashboardService.getMetricsForUser(username));
    }
}
