package com.roles.usermanagement.modules.asset.entity;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-23 - Vinculación de activo a ticket] - Asocia el equipo que presenta la falla directamente al ticket - Conecta con: Ticket, Asset y AssetService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_asset_links", indexes = {
        @Index(name = "idx_asset_link_ticket", columnList = "ticket_id"),
        @Index(name = "idx_asset_link_asset", columnList = "asset_id")
})
public class TicketAssetLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "linked_at", nullable = false, updatable = false)
    private LocalDateTime linkedAt;

    @PrePersist
    protected void onCreate() {
        this.linkedAt = LocalDateTime.now();
    }
}
