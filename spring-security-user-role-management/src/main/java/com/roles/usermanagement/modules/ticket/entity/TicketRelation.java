package com.roles.usermanagement.modules.ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-07/T-28 - Vinculación y fusión de tickets relacionados] - Enlaza tickets dependientes o duplicados con un tipo de relación - Conecta con: Ticket y TicketLinkService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_relations", indexes = {
        @Index(name = "idx_relation_source", columnList = "source_ticket_id"),
        @Index(name = "idx_relation_target", columnList = "target_ticket_id")
})
public class TicketRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_ticket_id", nullable = false)
    private Ticket sourceTicket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_ticket_id", nullable = false)
    private Ticket targetTicket;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String relationType = "RELATED_TO"; // RELATED_TO, DUPLICATE_OF, BLOCKS, BLOCKED_BY

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.relationType == null) this.relationType = "RELATED_TO";
    }
}
