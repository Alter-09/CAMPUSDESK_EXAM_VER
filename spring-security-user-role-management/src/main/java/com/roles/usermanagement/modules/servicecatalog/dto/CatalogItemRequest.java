package com.roles.usermanagement.modules.servicecatalog.dto;

import jakarta.validation.constraints.NotBlank;

// [Nueva Funcionalidad: A-13 - Petición de ítem de catálogo de servicios] - Parámetros para configurar servicios solicitables - Conecta con: ServiceCatalogItem
public record CatalogItemRequest(
        @NotBlank(message = "El nombre del servicio es obligatorio")
        String name,

        @NotBlank(message = "La descripción es obligatoria")
        String description,

        String category,
        String icon,
        Integer estimatedHours,
        String formSchema,
        Boolean isActive,
        Integer displayOrder
) {
}
