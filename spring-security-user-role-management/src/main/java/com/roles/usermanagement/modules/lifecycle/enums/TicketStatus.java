package com.roles.usermanagement.modules.lifecycle.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TicketStatus {
    // [Nueva Funcionalidad: T-01/A-05 - Estados de ciclo de vida ampliado] - Soporta pausas, pendientes de terceros, cancelación y escalación - Conecta con: Ticket y TicketLifecycleService
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    PENDING_USER,
    PENDING_VENDOR,
    CANCELLED,
    ESCALATED,
    RESOLVED,
    CLOSED;

    @JsonCreator
    public static TicketStatus fromString(String value) {
        if (value == null) return null;
        String v = value.trim().toUpperCase();
        return switch (v) {
            case "ABIERTA", "OPEN" -> OPEN;
            case "ASIGNADA", "ASSIGNED" -> ASSIGNED;
            case "EN_PROCESO", "IN_PROGRESS" -> IN_PROGRESS;
            case "PENDIENTE_USUARIO", "PENDING_USER" -> PENDING_USER;
            case "PENDIENTE_PROVEEDOR", "PENDING_VENDOR" -> PENDING_VENDOR;
            case "CANCELADA", "CANCELLED" -> CANCELLED;
            case "ESCALADA", "ESCALATED" -> ESCALATED;
            case "RESUELTA", "RESOLVED" -> RESOLVED;
            case "CERRADA", "CLOSED" -> CLOSED;
            default -> valueOf(v);
        };
    }
}