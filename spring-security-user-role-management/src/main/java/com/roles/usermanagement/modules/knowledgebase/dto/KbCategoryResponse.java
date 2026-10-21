package com.roles.usermanagement.modules.knowledgebase.dto;

// [Nueva Funcionalidad: A-04/U-08 - Respuesta de categoría KB] - Datos de categorías de artículos de ayuda - Conecta con: KbCategory
public record KbCategoryResponse(
        Long id,
        String name,
        String description,
        String icon,
        Integer displayOrder,
        Boolean isActive
) {
}
