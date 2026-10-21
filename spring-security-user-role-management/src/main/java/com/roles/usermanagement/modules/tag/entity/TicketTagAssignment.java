package com.roles.usermanagement.modules.tag.entity;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-22/T-16 - Asignación de etiquetas a tickets] - Relaciona un ticket con múltiples tags del sistema - Conecta con: Ticket, TicketTag y TicketTagService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_tag_assignments", indexes = {
        @Index(name = "idx_tag_assign_ticket", columnList = "ticket_id"),
        @Index(name = "idx_tag_assign_tag", columnList = "tag_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_ticket_tag", columnNames = {"ticket_id", "tag_id"})
})
public class TicketTagAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private TicketTag tag;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }
}
