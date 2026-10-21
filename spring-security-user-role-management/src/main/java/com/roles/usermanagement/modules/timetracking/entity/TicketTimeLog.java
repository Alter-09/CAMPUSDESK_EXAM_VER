package com.roles.usermanagement.modules.timetracking.entity;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-02/A-15 - Registro de tiempo de trabajo por ticket] - Permite cronometrar o imputar horas trabajadas por técnicos - Conecta con: Ticket y TimeTrackingService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_time_logs", indexes = {
        @Index(name = "idx_timelog_ticket", columnList = "ticket_id"),
        @Index(name = "idx_timelog_technician", columnList = "technician_username")
})
public class TicketTimeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "technician_username", nullable = false, length = 50)
    private String technicianUsername;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(length = 500)
    private String description;

    @Column(name = "logged_at", nullable = false, updatable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    protected void onCreate() {
        this.loggedAt = LocalDateTime.now();
    }
}
