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
@RequestMapping({"/api/tickets/{ticketId}/transitions", "/api/tickets/{ticketId}/status"})
@Tag(name = "Ticket lifecycle", description = "Cambios de estado de un ticket (RF-04, RT-03)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TicketLifecycleController {

    private final TicketLifecycleService lifecycleService;

    // Constructor injection
    public TicketLifecycleController(TicketLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @org.springframework.web.bind.annotation.RequestMapping(
            method = {org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.PATCH}
    )
    @Operation(summary = "Ejecutar un cambio de estado o acción sobre el ticket (PATCH /status o POST /transitions)",
            description = "ASSIGN/REASSIGN: solo ADMIN (requiere technicianUsername). "
                    + "START_PROGRESS/RESOLVE: solo el técnico asignado. "
                    + "CONFIRM_CLOSE: solo el solicitante. El backend valida el flujo y los permisos.")
    public StateTransitionResponse transition(@PathVariable Long ticketId,
                                              @Valid @RequestBody StateTransitionRequest request,
                                              Authentication auth) {
        return lifecycleService.transition(ticketId, request, auth);
    }
}