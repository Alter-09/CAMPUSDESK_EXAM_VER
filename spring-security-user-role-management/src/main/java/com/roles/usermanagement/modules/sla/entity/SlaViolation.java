package com.roles.usermanagement.modules.sla.entity;

import com.roles.usermanagement.modules.sla.enums.SlaBreachType;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: A-08/T-18 - Registro de incumplimientos de SLA] - Registra cuando un ticket supera el tiempo estipulado de respuesta o resolución - Conecta con: Ticket y SlaService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sla_violations")
public class SlaViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "breach_type", nullable = false, length = 30)
    private SlaBreachType breachType;

    @Column(name = "breached_at", nullable = false)
    private LocalDateTime breachedAt;

    @Column(name = "escalated_to", length = 100)
    private String escalatedTo;

    @Column(length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (this.breachedAt == null) {
            this.breachedAt = LocalDateTime.now();
        }
    }
}
