package com.roles.usermanagement.modules.ticket.repository;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import org.springframework.data.jpa.domain.Specification;

public final class TicketSpecifications {

    // Specifications class to avoid multiple query methods by status, priority, or category, simplifying dynamic filtering

    private TicketSpecifications(){}

    public static Specification<Ticket> hasStatus(TicketStatus status){
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasPriority(TicketPriority priority){
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> hasCategory(TicketCategory category){
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Ticket> appliedBy(String username) {
        return (root, query, cb) ->
                cb.equal(root.get("applicant").get("username"), username);
    }

    public static Specification<Ticket> assignedTo(String username) {
        return (root, query, cb) ->
                cb.equal(root.get("technician").get("username"), username);
    }

    // [Nueva Funcionalidad: U-17 - Filtro de tickets por rango de fechas] - Filtra tickets creados entre dos fechas - Conecta con: TicketRepository y TicketService
    public static Specification<Ticket> createdBetween(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    // [Nueva Funcionalidad: T-22 - Búsqueda de tickets por palabra clave] - Búsqueda libre en título o descripción de tickets - Conecta con: TicketRepository
    public static Specification<Ticket> containsText(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    // [Nueva Funcionalidad: U-26 - Filtro por ítem de catálogo de servicios] - Filtra tickets originados desde un servicio específico - Conecta con: ServiceCatalogItem
    public static Specification<Ticket> withCatalogId(Long catalogId) {
        return (root, query, cb) ->
                catalogId == null ? null : cb.equal(root.get("serviceCatalogId"), catalogId);
    }
}
