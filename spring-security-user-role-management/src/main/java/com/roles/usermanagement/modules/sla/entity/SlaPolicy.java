package com.roles.usermanagement.modules.sla.entity;

import com.roles.usermanagement.modules.sla.enums.SlaPriority;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: A-01/T-15 - Entidad de políticas de SLA] - Define tiempos máximos de primera respuesta y resolución por prioridad - Conecta con: SlaViolation y SlaService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, unique = true)
    private SlaPriority priority;

    @Column(name = "first_response_hours", nullable = false)
    private Integer firstResponseHours;

    @Column(name = "resolution_hours", nullable = false)
    private Integer resolutionHours;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
