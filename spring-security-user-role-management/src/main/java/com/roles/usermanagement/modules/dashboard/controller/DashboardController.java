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
 * Adapts authenticated HTTP requests and dashboard use-case responses to the
 * API contract types.
 */
@RestController
@RequestMapping("/api/dashboard")
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
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardMetricsResponse> getDashboardStats(
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(dashboardMapper.toResponse(dashboardService.getMetricsForUser(username)));
    }
}
