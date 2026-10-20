package com.roles.usermanagement.modules.asset.dto;

import com.roles.usermanagement.modules.asset.enums.AssetStatus;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-23/A-27 - Respuesta de activo IT] - Expone información de inventario de equipos - Conecta con: Asset
public record AssetResponse(
        Long id,
        String name,
        String serialNumber,
        String category,
        AssetStatus status,
        String assignedToUsername,
        String location,
        LocalDateTime createdAt
) {
}
