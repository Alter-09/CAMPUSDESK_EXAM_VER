package com.roles.usermanagement.modules.tag.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-22/A-23 - Etiquetas/Tags del sistema] - Catálogo de tags para clasificación transversal de tickets - Conecta con: TicketTagAssignment y TicketTagService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_tags")
public class TicketTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(length = 20)
    @Builder.Default
    private String color = "#3b82f6";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.color == null) this.color = "#3b82f6";
    }
}
