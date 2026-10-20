package com.roles.usermanagement.modules.tag.dto;

// [Nueva Funcionalidad: U-22/A-23 - Respuesta de etiqueta] - Expone datos de tag y color a la interfaz - Conecta con: TicketTag
public record TagResponse(
        Long id,
        String name,
        String color
) {
}
