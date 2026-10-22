package com.roles.usermanagement.modules.sla.dto;

import com.roles.usermanagement.modules.sla.enums.SlaPriority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SlaPolicyRequest(
        @NotBlank(message = "El nombre de la política SLA es obligatorio")
        String name,
        @NotNull(message = "La prioridad de la política a crear es obligatoria")
        SlaPriority priority,
        @NotNull(message = "El tiempo para primera respuesta es obligatorio")
        @Min(value = 1, message = "Las horas deben ser al menos 1 de respuesta")
        Integer firstResponseHours,
        @NotNull(message = "El tiempo de resolución es obligatorio")
        @Min(value = 1, message = "Las horas deben ser al menos 1 para resolución")
        Integer resolutionHours,
        Boolean isActive
) {
}
