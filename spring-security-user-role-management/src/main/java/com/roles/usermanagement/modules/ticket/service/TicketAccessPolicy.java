package com.roles.usermanagement.modules.ticket.service;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// @Component: Spring creates an instance and injects it via constructor
@Component
public class TicketAccessPolicy {

    /** Role names centralized in one place; Spring stores them with ROLE_ prefix */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";

    /** Is the authenticated user an administrator? */
    public boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        if ("superadmin".equalsIgnoreCase(auth.getName())) return true;
        return auth.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority())
                        || "ROLE_SUPERADMIN".equals(authority.getAuthority()));
    }

    /** Is the authenticated user a technician? */
    public boolean isTechnician(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> ROLE_TECHNICIAN.equals(authority.getAuthority()));
    }

    /** Did the authenticated user create the ticket? */
    public boolean isApplicant(Ticket ticket, Authentication auth) {
        // Compares applicant username with the requesting user
        return auth != null && ticket.getApplicant().getUsername().equalsIgnoreCase(auth.getName());
    }

    /** Is the authenticated user the assigned technician for the ticket? */
    public boolean isAssignedTechnician(Ticket ticket, Authentication auth) {
        // First checks technician presence (can be null if ticket is OPEN)
        return auth != null && ticket.getTechnician() != null
                // Short-circuit prevents NullPointerException if technician is null
                && ticket.getTechnician().getUsername().equalsIgnoreCase(auth.getName());
    }

    /** Can view ticket? Admin, requester, or assigned technician */
    public boolean canView(Ticket ticket, Authentication auth) {
        // || operator: true if any of the three conditions is satisfied
        return isAdmin(auth) || isApplicant(ticket, auth) || isAssignedTechnician(ticket, auth);
    }

    /** If user CANNOT view ticket, halts execution throwing 403 */
    public void assertCanView(Ticket ticket, Authentication auth) {
        // Logical NOT: "if CANNOT view"
        if (!canView(ticket, auth)) {
            // 403 = authenticated session, but forbidden for this resource
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket");
        }
        // If permitted, execution continues uninterrupted
    }

    // Indicates whether authenticated user has the specified role (e.g. "ROLE_ADMIN")
    public boolean hasRole(Authentication auth, String role) {
        if (auth == null) return false;
        if (isAdmin(auth)) return true; // Superadmin / Admin has all role capabilities
        return auth.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }
}