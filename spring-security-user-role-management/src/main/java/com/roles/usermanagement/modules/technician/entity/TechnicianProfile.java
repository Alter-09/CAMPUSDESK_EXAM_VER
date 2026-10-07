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
    private Integer activeTicketsCount = 0;

    @Column(name = "specialty", length = 100)
    private String specialty; // e.g. "REDES", "HARDWARE", "SISTEMAS"

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
