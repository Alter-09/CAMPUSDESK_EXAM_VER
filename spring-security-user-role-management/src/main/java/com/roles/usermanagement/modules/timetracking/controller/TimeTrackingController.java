package com.roles.usermanagement.modules.timetracking.controller;

import com.roles.usermanagement.modules.timetracking.dto.TimeLogRequest;
import com.roles.usermanagement.modules.timetracking.dto.TimeLogResponse;
import com.roles.usermanagement.modules.timetracking.service.TimeTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// [Nueva Funcionalidad: T-02/A-15 - Controlador REST de registro de tiempos] - Endpoints para imputar horas y consultar tiempos dedicados por ticket - Conecta con: TimeTrackingService
@RestController
@RequestMapping("/api/v1/timetracking")
@RequiredArgsConstructor
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    @PostMapping("/tickets/{ticketId}/logs")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    public ResponseEntity<TimeLogResponse> logTime(
            @PathVariable Long ticketId,
            @Valid @RequestBody TimeLogRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeTrackingService.logTime(ticketId, request, auth.getName()));
    }

    @GetMapping("/tickets/{ticketId}/logs")
    public ResponseEntity<List<TimeLogResponse>> getLogs(@PathVariable Long ticketId) {
        return ResponseEntity.ok(timeTrackingService.getTimeLogs(ticketId));
    }

    @GetMapping("/tickets/{ticketId}/total-minutes")
    public ResponseEntity<Map<String, Integer>> getTotalMinutes(@PathVariable Long ticketId) {
        return ResponseEntity.ok(Map.of("totalMinutes", timeTrackingService.getTotalMinutes(ticketId)));
    }
}
