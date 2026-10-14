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
}
