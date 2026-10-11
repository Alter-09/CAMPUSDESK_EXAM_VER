package com.roles.usermanagement.domain.service;

public class UserRoles {
    public enum Role {
        ADMIN,
        TECHNICIAN,
        USER
    }

    public enum Authority {
        // Permisos de Usuarios y Seguridad
        USER_READ,
        USER_CREATE,
        USER_UPDATE,
        USER_DELETE,
        ROLE_ASSIGN,
        PERMISSION_ASSIGN,
        ROLE_MANAGE,
        PERMISSION_MANAGE,

        // Permisos de Tickets para Solicitantes (USER)
        TICKET_CREATE,
        TICKET_READ_OWN,
        TICKET_UPDATE_OWN,
        TICKET_CLOSE,

        // Permisos de Técnicos (TECHNICIAN)
        TICKET_READ_ASSIGNED,
        TICKET_STATUS_UPDATE,

        // Permisos Administrativos (ADMIN)
        TICKET_READ_ALL,
        TICKET_ASSIGN,
        REPORT_READ,

        // Permisos Comunes (Comentarios e Historial)
        COMMENT_CREATE,
        COMMENT_READ,
        HISTORY_READ;

        public String value() {
            return name();
        }
    }
}