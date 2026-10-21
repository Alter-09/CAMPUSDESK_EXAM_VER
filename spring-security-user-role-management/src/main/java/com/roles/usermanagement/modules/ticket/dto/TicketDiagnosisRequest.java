package com.roles.usermanagement.modules.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para registrar diagnóstico técnico, causa raíz y resumen de solución (T-24).
 */
public record TicketDiagnosisRequest(
        @NotBlank(message = "La causa raíz es obligatoria")
        @Size(max = 2000, message = "La causa raíz no puede superar 2000 caracteres")
        String rootCause,

        @NotBlank(message = "El resumen de resolución es obligatorio")
        @Size(max = 2000, message = "El resumen no puede superar 2000 caracteres")
        String resolutionSummary
) {}
