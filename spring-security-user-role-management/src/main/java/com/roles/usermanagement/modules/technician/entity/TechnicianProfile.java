package com.roles.usermanagement.modules.technician.entity;
import com.roles.usermanagement.modules.technician.enums.TechnicianAvailabilityStatus;
import com.roles.usermanagement.persistance.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "technician_profiles")
public class TechnicianProfile {

    @Id
    @Column(name = "username", length = 50)
    private String username;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "username", referencedColumnName = "username")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 25)
    private TechnicianAvailabilityStatus availabilityStatus;

    @Column(name = "active_tickets_count", nullable = false)
    @Builder.Default
    private Integer activeTicketsCount = 0;

    @Column(name = "specialty", length = 100)
    private String specialty; // e.g. "REDES", "HARDWARE", "SISTEMAS"

    // [Nueva Funcionalidad: T-04/A-16/T-19 - Capacidad operativa, disponibilidad y habilidades técnicas] - Habilita auto-asignación por carga activa y balanceo - Conecta con: TicketAssignmentService
    @Column(name = "max_active_tickets", nullable = false)
    @Builder.Default
    private Integer maxActiveTickets = 5;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "technician_skills", joinColumns = @JoinColumn(name = "technician_username"))
    @Column(name = "skill")
    @Builder.Default
    private java.util.Set<String> skills = new java.util.HashSet<>();

    @Column(name = "last_assigned_at")
    private LocalDateTime lastAssignedAt;

    @PrePersist
    protected void onCreate() {
        if (this.availabilityStatus == null) {
            this.availabilityStatus = TechnicianAvailabilityStatus.AVAILABLE;
        }
        if (this.activeTicketsCount == null) {
            this.activeTicketsCount = 0;
        }
    }

}
