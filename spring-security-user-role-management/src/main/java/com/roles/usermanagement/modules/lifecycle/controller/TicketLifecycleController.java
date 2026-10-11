package com.roles.usermanagement.modules.lifecycle.controller;

import com.roles.usermanagement.modules.lifecycle.dto.StateTransitionRequest;
import com.roles.usermanagement.modules.lifecycle.dto.StateTransitionResponse;
import com.roles.usermanagement.modules.lifecycle.service.TicketLifecycleService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets/{ticketId}/transitions")
@Tag(name = "Ticket lifecycle", description = "Cambios de estado de un ticket (RF-04)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TicketLifecycleController {

    private final TicketLifecycleService lifecycleService;

    // Inyección por constructor
    public TicketLifecycleController(TicketLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @PostMapping
    @Operation(summary = "Ejecutar una acción sobre el ticket",
            description = "ASSIGN/REASSIGN: solo ADMIN (requiere technicianUsername). "
                    + "START_PROGRESS/RESOLVE: solo el técnico asignado. "
                    + "CONFIRM_CLOSE: solo el solicitante. El backend valida el flujo y los permisos.")
    public StateTransitionResponse transition(@PathVariable Long ticketId,
                                              @Valid @RequestBody StateTransitionRequest request,
                                              Authentication auth) {
        return lifecycleService.transition(ticketId, request, auth);
    }
}