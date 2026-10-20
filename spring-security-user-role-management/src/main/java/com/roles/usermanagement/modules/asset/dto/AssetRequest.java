package com.roles.usermanagement.modules.asset.dto;

import com.roles.usermanagement.modules.asset.enums.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// [Nueva Funcionalidad: A-27 - Petición de alta/edición de activo IT] - Datos para registro de equipos informáticos - Conecta con: Asset
public record AssetRequest(
        @NotBlank(message = "El nombre del equipo es obligatorio")
        String name,

        @NotBlank(message = "El número de serie es obligatorio")
        String serialNumber,

        @NotBlank(message = "La categoría es obligatoria")
        String category,

        AssetStatus status,
        String assignedToUsername,
        String location
) {
}
