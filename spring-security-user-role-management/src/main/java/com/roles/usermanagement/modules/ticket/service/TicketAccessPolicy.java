package com.roles.usermanagement.modules.ticket.service;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// @Component: Spring crea una instancia y la entrega a quien la pida por constructor
@Component
public class TicketAccessPolicy {

    /** Nombres de rol guardados en un solo lugar; Spring los guarda con el prefijo ROLE_ */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";

    /** ¿El usuario autenticado es administrador? */
    public boolean isAdmin(Authentication auth) {
        // getAuthorities(): lista de roles y permisos del usuario
        return auth.getAuthorities().stream()
                // anyMatch: true si AL MENOS UNO cumple la condición
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }

    /** ¿El usuario autenticado es técnico? */
    public boolean isTechnician(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> ROLE_TECHNICIAN.equals(authority.getAuthority()));
    }

    /** ¿El usuario autenticado fue quien creó el ticket? */
    public boolean isApplicant(Ticket ticket, Authentication auth) {
        // Compara el username del solicitante del ticket con el de quien hace la petición
        return ticket.getApplicant().getUsername().equals(auth.getName());
    }

    /** ¿El usuario autenticado es el técnico asignado al ticket? */
    public boolean isAssignedTechnician(Ticket ticket, Authentication auth) {
        // Primero se verifica que haya técnico (puede ser null si el ticket está OPEN)
        return ticket.getTechnician() != null
                // El && corta aquí si es null, así evitamos un NullPointerException
                && ticket.getTechnician().getUsername().equals(auth.getName());
    }

    /** ¿Puede ver el ticket? Admin, o solicitante, o técnico asignado */
    public boolean canView(Ticket ticket, Authentication auth) {
        // || significa "o": basta con que una de las tres se cumpla
        return isAdmin(auth) || isApplicant(ticket, auth) || isAssignedTechnician(ticket, auth);
    }

    /** Si NO puede ver el ticket, detiene todo lanzando un error 403 */
    public void assertCanView(Ticket ticket, Authentication auth) {
        // El ! invierte el resultado: "si NO puede ver"
        if (!canView(ticket, auth)) {
            // 403 = tienes sesión, pero no tienes permiso para esto
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket");
        }
        // Si puede, no hace nada y el método que lo llamó continúa
    }
}