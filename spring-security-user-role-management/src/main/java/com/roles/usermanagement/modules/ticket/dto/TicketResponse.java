package com.roles.usermanagement.modules.ticket.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        String applicantUsername,
        String technicianUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        // [Nueva Funcionalidad: U-04/T-18/U-26/U-28 - Atributos de respuesta extendidos de ticket] - Expone SLA, catálogo, urgencia y reaperturas a la API - Conecta con: TicketMapper
        Long serviceCatalogId,
        LocalDateTime slaDueDate,
        LocalDateTime firstResponseDueDate,
        LocalDateTime firstResponseAt,
        LocalDateTime resolvedAt,
        Integer reopenedCount,
        Boolean isEscalated,
        String urgentReason,
        String rootCause,
        String resolutionSummary,
        Set<String> tags
) {
    public TicketResponse(
            Long id,
            String title,
            String description,
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status,
            String applicantUsername,
            String technicianUsername,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long serviceCatalogId,
            LocalDateTime slaDueDate,
            LocalDateTime firstResponseDueDate,
            LocalDateTime firstResponseAt,
            LocalDateTime resolvedAt,
            Integer reopenedCount,
            Boolean isEscalated,
            String urgentReason,
            String rootCause,
            String resolutionSummary
    ) {
        this(id, title, description, category, priority, status, applicantUsername, technicianUsername,
                createdAt, updatedAt, serviceCatalogId, slaDueDate, firstResponseDueDate, firstResponseAt,
                resolvedAt, reopenedCount, isEscalated, urgentReason, rootCause, resolutionSummary, Collections.emptySet());
    }
}