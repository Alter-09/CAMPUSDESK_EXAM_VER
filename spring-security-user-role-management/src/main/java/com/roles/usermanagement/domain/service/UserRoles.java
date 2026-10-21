package com.roles.usermanagement.domain.service;

import java.util.Set;

public class UserRoles {
    public enum Role {
        SUPERADMIN,
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

        // Permiso exclusivo para SuperAdmin: borrado definitivo de incidencias
        TICKET_DELETE,

        // Common Permissions (Comments and History)
        COMMENT_CREATE,
        COMMENT_READ,
        HISTORY_READ;

        public String value() {
            return name();
        }
    }

    /**
     * Permisos exclusivos de SUPERADMIN.
     * El rol ADMIN operativo NO tiene acceso a estas operaciones críticas del sistema.
     */
    public static final Set<Authority> SUPERADMIN_EXCLUSIVE_AUTHORITIES = Set.of(
            Authority.USER_DELETE,
            Authority.TICKET_DELETE,
            Authority.ROLE_MANAGE,
            Authority.PERMISSION_MANAGE,
            Authority.PERMISSION_ASSIGN,
            Authority.AUDIT_READ
    );

    /**
     * Determina si una autoridad corresponde a las facultades operativas del rol ADMIN.
     */
    public static boolean isAdminAuthority(Authority authority) {
        return authority != null && !SUPERADMIN_EXCLUSIVE_AUTHORITIES.contains(authority);
    }
}