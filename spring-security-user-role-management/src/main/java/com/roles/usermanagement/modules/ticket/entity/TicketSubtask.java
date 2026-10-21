package com.roles.usermanagement.modules.ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-06 - Sub-tareas dentro del ticket] - Permite descomponer un ticket en tareas atómicas con estado independiente - Conecta con: Ticket y TicketSubtaskService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_subtasks", indexes = {
        @Index(name = "idx_subtask_ticket", columnList = "ticket_id")
})
public class TicketSubtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "assigned_to", length = 50)
    private String assignedTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isCompleted == null) this.isCompleted = false;
    }
}
