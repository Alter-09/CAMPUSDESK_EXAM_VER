package com.roles.usermanagement.modules.statushistory.controller;

import com.roles.usermanagement.modules.statushistory.dto.StatusHistoryResponse;
import com.roles.usermanagement.modules.statushistory.service.StatusHistoryService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de solo lectura para consultar el historial de estados de un ticket.
 * No expone operaciones de creación, edición ni borrado: el historial es inmodificable.
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
@Tag(name = "Status history", description = "Historial de cambios de estado de un ticket")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class StatusHistoryController {

    private final StatusHistoryService historyService;

    // Spring inyecta el service por constructor
    public StatusHistoryController(StatusHistoryService historyService) {
        this.historyService = historyService;
    }

    /** Devuelve el historial del ticket en orden cronológico. */
    @GetMapping
    @Operation(summary = "Get the status history of a ticket")
    public List<StatusHistoryResponse> getHistory(@PathVariable Long ticketId,
                                                  Authentication auth) {
        return historyService.getByTicket(ticketId, auth);
    }
}