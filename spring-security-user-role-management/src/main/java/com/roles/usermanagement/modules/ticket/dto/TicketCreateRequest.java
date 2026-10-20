package com.roles.usermanagement.modules.ticket.dto;

import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(

        @NotBlank(message = "the title is obligatory")
        @Size(min = 5, max = 150, message = "The title must be between 5 and 150 characters long.")
        String title,

        @NotBlank(message = "The description is mandatory.")
        @Size(min = 10, max = 2000, message = "The description must be between 10 and 2,000 characters long")
        String description,

        @NotNull(message = "The category is mandatory.")
        TicketCategory category,

        @NotNull(message = "The priority is mandatory.")
        TicketPriority priority,

        // [Nueva Funcionalidad: U-26/U-28 - Asociación de catálogo y justificación de urgencia en creación] - Permite asociar ítems de catálogo y justificar prioridad crítica - Conecta con: Ticket
        Long serviceCatalogId,
        String urgentReason
) {
}