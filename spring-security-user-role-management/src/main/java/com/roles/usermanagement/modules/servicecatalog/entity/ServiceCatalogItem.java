package com.roles.usermanagement.modules.servicecatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-26/A-13/U-27 - Catálogo de servicios IT] - Elementos de servicio estructurados con esquemas dinámicos para solicitudes - Conecta con: Ticket y ServiceCatalogService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "service_catalog_items")
public class ServiceCatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category; // HARDWARE, SOFTWARE, CUENTAS_ACCESOS, REDES, TELEFONIA

    @Column(length = 50)
    private String icon;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "form_schema", columnDefinition = "TEXT") // JSON representation of dynamic fields
    private String formSchema;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
        if (this.displayOrder == null) this.displayOrder = 0;
    }
}
