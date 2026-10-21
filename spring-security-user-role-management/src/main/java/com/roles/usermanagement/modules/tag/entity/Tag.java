package com.roles.usermanagement.modules.tag.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Tag para clasificación transversal de incidencias (Ejercicio 1 Examen).
 * Mapeo ManyToMany bidireccional con Ticket.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre;

    @JsonIgnore
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Ticket> tickets = new HashSet<>();

    public Tag(String nombre) {
        this.nombre = nombre != null ? nombre.trim().toUpperCase() : null;
    }

    @PrePersist
    @PreUpdate
    protected void onNormalize() {
        if (this.nombre != null) {
            this.nombre = this.nombre.trim().toUpperCase();
        }
    }
}
