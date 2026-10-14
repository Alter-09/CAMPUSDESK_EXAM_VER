package com.roles.usermanagement.modules.statushistory.entity;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.statushistory.enums.StatusHistoryAction;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.persistance.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_status_history", indexes = {
        @Index(name = "idx_history_ticket_changed", columnList = "ticket_id, changed_at")
})

public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private TicketStatus previousStatus; // Null at initial creation

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private TicketStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_username", referencedColumnName = "username", nullable = false)
    private UserEntity changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 30)
    private StatusHistoryAction action;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }

}
