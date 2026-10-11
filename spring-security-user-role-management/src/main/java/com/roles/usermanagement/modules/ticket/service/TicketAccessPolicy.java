package com.roles.usermanagement.modules.ticket.service;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TicketAccessPolicy {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** Returns true if the authenticated user has the ADMIN role. */
    public boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }

    /** Returns true if the authenticated user created the ticket. */
    public boolean isApplicant(Ticket ticket, Authentication auth) {
        return ticket.getApplicant().getUsername().equals(auth.getName());
    }

    /** Returns true if the authenticated user is the technician assigned to the ticket. */
    public boolean isAssignedTechnician(Ticket ticket, Authentication auth) {
        return ticket.getTechnician() != null
                && ticket.getTechnician().getUsername().equals(auth.getName());
    }

    /** Returns true if the user is an admin, the applicant, or the assigned technician. */
    public boolean canView(Ticket ticket, Authentication auth) {
        return isAdmin(auth) || isApplicant(ticket, auth) || isAssignedTechnician(ticket, auth);
    }

    /** Throws 403 Forbidden if the user cannot view the ticket; does nothing otherwise. */
    public void assertCanView(Ticket ticket, Authentication auth) {
        if (!canView(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket");
        }
    }
}