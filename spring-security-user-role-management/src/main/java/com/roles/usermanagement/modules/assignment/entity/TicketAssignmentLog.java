package com.roles.usermanagement.modules.assignment.entity;

import com.roles.usermanagement.modules.assignment.enums.AssignmentType;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.persistance.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registra la asignación inicial o reasignación de un técnico a una incidencia
 * para mantener su trazabilidad y auditoría (RF-04).
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

    /** Incidencia cuyo técnico fue asignado o reasignado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Técnico anterior; no tiene valor en una asignación inicial. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_technician_username", referencedColumnName = "username")
    private UserEntity previousTechnician;

    /** Técnico que queda responsable de la incidencia. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "new_technician_username", referencedColumnName = "username", nullable = false)
    private UserEntity newTechnician;

    /** Usuario que ejecutó la operación administrativa. */
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

    /** Establece la fecha de auditoría al crear el registro. */
    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }

}
