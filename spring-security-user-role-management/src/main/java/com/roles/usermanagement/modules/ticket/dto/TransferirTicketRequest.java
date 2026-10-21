package com.roles.usermanagement.modules.ticket.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la transferencia de ticket entre áreas / categorías (Ejercicio 3 Examen).
 */
public record TransferirTicketRequest(
        @NotNull(message = "La nueva categoría es obligatoria")
        TicketCategory nuevaCategoria,

        @NotBlank(message = "El motivo o justificación de la transferencia es obligatorio")
        @Size(max = 500, message = "No puede superar 500 caracteres")
        @JsonAlias({"motivoTransferencia", "motivo"})
        String justificacion
) {
    public String motivoTransferencia() {
        return justificacion;
    }
}

