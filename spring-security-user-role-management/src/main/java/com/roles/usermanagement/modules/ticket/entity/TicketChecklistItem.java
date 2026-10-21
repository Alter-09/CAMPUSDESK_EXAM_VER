package com.roles.usermanagement.modules.ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-30 - Checklist de pasos de resolución] - Pasos de verificación requeridos antes de cerrar o resolver el ticket - Conecta con: Ticket y TicketChecklistService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_checklist_items", indexes = {
        @Index(name = "idx_checklist_ticket", columnList = "ticket_id")
})
public class TicketChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "is_checked", nullable = false)
    @Builder.Default
    private Boolean isChecked = false;

    @Column(name = "checked_by", length = 50)
    private String checkedBy;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @PrePersist
    protected void onCreate() {
        if (this.isChecked == null) this.isChecked = false;
    }
}
