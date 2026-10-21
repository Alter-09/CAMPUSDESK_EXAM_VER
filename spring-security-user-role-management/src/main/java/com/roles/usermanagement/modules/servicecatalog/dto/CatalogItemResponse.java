package com.roles.usermanagement.modules.servicecatalog.dto;

import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-26/A-13 - Respuesta de ítem de catálogo de servicios] - Información del servicio IT y su formulario dinámico - Conecta con: ServiceCatalogItem
public record CatalogItemResponse(
        Long id,
        String name,
        String description,
        String category,
        String icon,
        Integer estimatedHours,
        String formSchema,
        Boolean isActive,
        Integer displayOrder,
        LocalDateTime createdAt
) {
}
