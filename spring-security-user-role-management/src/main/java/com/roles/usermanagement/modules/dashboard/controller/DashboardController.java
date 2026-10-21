package com.roles.usermanagement.modules.dashboard.controller;

import com.roles.usermanagement.modules.dashboard.dto.DashboardMetricsResponse;
import com.roles.usermanagement.modules.dashboard.mapper.DashboardMapper;
import com.roles.usermanagement.modules.dashboard.service.DashboardService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapts authenticated HTTP requests and dashboard use-case responses to the
 * API contract types.
 */
@RestController
@Tag(name = "Dashboard", description = "Métricas operativas y estadísticas en tiempo real según rol del usuario")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardMapper dashboardMapper;

    public DashboardController(DashboardService dashboardService, DashboardMapper dashboardMapper) {
        this.dashboardService = dashboardService;
        this.dashboardMapper = dashboardMapper;
    }

    /**
     * Returns statistics according to the authenticated user's scope.
     */
    @GetMapping({"/api/dashboard/stats", "/api/reports/summary"})
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar métricas y estadísticas del dashboard según alcance del rol (MOD-39, U-25, T-10, A-07, A-30)")
    public ResponseEntity<DashboardMetricsResponse> getDashboardStats(
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(dashboardMapper.toResponse(dashboardService.getMetricsForUser(username)));
    }
}
