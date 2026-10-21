package com.roles.usermanagement.modules.timetracking.entity;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.persistance.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad TicketWorklog para bitácora de registro de tiempo técnico (Ejercicio 4 Examen).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_worklogs", indexes = {
        @Index(name = "idx_worklog_ticket", columnList = "ticket_id"),
        @Index(name = "idx_worklog_tech", columnList = "user_id")
})
public class TicketWorklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "minutos_dedicados", nullable = false)
    private Integer minutosDedicados;

    @Column(name = "descripcion_actividad", nullable = false, columnDefinition = "TEXT")
    private String descripcionActividad;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "username", nullable = false)
    private UserEntity technician;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}
