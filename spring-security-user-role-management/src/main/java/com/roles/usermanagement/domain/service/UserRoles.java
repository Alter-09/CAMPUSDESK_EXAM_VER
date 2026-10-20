package com.roles.usermanagement.domain.service;

public class UserRoles {
    public enum Role {
        ADMIN,
        TECHNICIAN,
        USER
    }

    public enum Authority {
        // User and Security Permissions
        USER_READ,
        USER_CREATE,
        USER_UPDATE,
        USER_DELETE,
        ROLE_ASSIGN,
        PERMISSION_ASSIGN,
        ROLE_MANAGE,
        PERMISSION_MANAGE,

        // Ticket Permissions for Requesters (USER)
        TICKET_CREATE,
        TICKET_READ_OWN,
        TICKET_UPDATE_OWN,
        TICKET_CLOSE,

        // Technician Permissions (TECHNICIAN)
        TICKET_READ_ASSIGNED,
        TICKET_STATUS_UPDATE,

        // Administrative Permissions (ADMIN)
        TICKET_READ_ALL,
        TICKET_ASSIGN,
        REPORT_READ,

        // [Nueva Funcionalidad: A-21/A-26/A-01 - Autoridades de permisos finos] - Control granular de asignación masiva, reportes, auditoría y catálogo - Conecta con: SecurityConfig y PreAuthorize
        TICKET_BULK_UPDATE,
        TICKET_ESCALATE,
        SLA_CONFIG_WRITE,
        REPORT_EXPORT,
        AUDIT_READ,
        SERVICE_CATALOG_ADMIN,

        // Common Permissions (Comments and History)
        COMMENT_CREATE,
        COMMENT_READ,
        HISTORY_READ;

        public String value() {
            return name();
        }
    }
}