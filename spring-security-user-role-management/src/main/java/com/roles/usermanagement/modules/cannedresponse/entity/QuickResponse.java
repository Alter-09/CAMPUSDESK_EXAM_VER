package com.roles.usermanagement.modules.cannedresponse.entity;

import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.persistance.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad QuickResponse para plantillas de solución rápida de técnicos (Ejercicio 5 Examen).
 * Título único por técnico y categoría de ticket asociada.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quick_responses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_quick_response_user_title", columnNames = {"technician_username", "titulo"})
}, indexes = {
        @Index(name = "idx_quick_response_tech", columnList = "technician_username")
})
public class QuickResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_username", referencedColumnName = "username", nullable = false)
    private UserEntity technician;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
