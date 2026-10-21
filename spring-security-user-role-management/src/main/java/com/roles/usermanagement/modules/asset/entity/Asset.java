package com.roles.usermanagement.modules.asset.entity;

import com.roles.usermanagement.modules.asset.enums.AssetStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-23/A-27 - Inventario de activos IT] - Registro de hardware y software asignado a usuarios institucionales - Conecta con: TicketAssetLink y AssetService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_asset_user", columnList = "assigned_to_username")
})
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "serial_number", length = 100, unique = true)
    private String serialNumber;

    @Column(nullable = false, length = 50)
    private String category; // LAPTOP, DESKTOP, MONITOR, PRINTER, NETWORK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private AssetStatus status = AssetStatus.IN_USE;

    @Column(name = "assigned_to_username", length = 50)
    private String assignedToUsername;

    @Column(length = 100)
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = AssetStatus.IN_USE;
    }
}
