package com.roles.usermanagement.modules.assignment.entity;

import com.roles.usermanagement.modules.assignment.enums.AssignmentType;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.persistance.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records the initial assignment or reassignment of a technician to a ticket
 * for traceability and auditing (RF-04).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_assignment_logs")
public class TicketAssignmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ticket whose technician was assigned or reassigned. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Previous technician; null for an initial assignment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_technician_username", referencedColumnName = "username")
    private UserEntity previousTechnician;

    /** Technician who is responsible for the ticket. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "new_technician_username", referencedColumnName = "username", nullable = false)
    private UserEntity newTechnician;

    /** User who performed the administrative operation. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by_username", referencedColumnName = "username", nullable = false)
    private UserEntity assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 20)
    private AssignmentType assignmentType;

    @Column(name = "justification", length = 300)
    private String justification;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    /** Sets the audit timestamp when the record is created. */
    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }

}
