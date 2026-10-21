package com.roles.usermanagement.modules.satisfaction.entity;

import com.roles.usermanagement.modules.satisfaction.enums.CsatScore;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-06/A-21 - Encuestas de satisfacción CSAT] - Calificación de 1 a 5 y comentarios del solicitante tras resolver el ticket - Conecta con: Ticket y SatisfactionService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_satisfactions", indexes = {
        @Index(name = "idx_csat_ticket", columnList = "ticket_id", unique = true)
})
public class TicketSatisfaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private CsatScore score;

    @Column(length = 1000)
    private String feedback;

    @Column(name = "submitted_by", nullable = false, length = 50)
    private String submittedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
