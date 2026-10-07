package com.roles.usermanagement.modules.lifecycle.entity;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;
import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_lifecycle_rules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_lifecycle_transition", columnNames = {"current_status", "target_status", "action"})
})
public class LifecycleRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private TicketStatus currentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_status", nullable = false, length = 30)
    private TicketStatus targetStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransitionAction action;

    @Column(name = "required_role", nullable = false, length = 50)
    private String requiredRole; // e.g. "ROLE_ADMIN", "ROLE_TECHNICIAN", "ROLE_USER"

    @Column(name = "must_be_applicant", nullable = false)
    private boolean mustBeApplicant = false;

    @Column(name = "must_be_assigned_technician", nullable = false)
    private boolean mustBeAssignedTechnician = false;
}


