package com.roles.usermanagement.modules.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de reapertura de un ticket resuelto (Ejercicio 2 Examen).
 */
public record ReaperturaRequest(
        @NotBlank(message = "El motivo de reapertura es obligatorio")
        @Size(max = 250, message = "El motivo no puede superar los 250 caracteres")
        String motivo
) {}
