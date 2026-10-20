package com.roles.usermanagement.modules.ticket.entity;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
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
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_status", columnList = "status"),
        @Index(name = "idx_ticket_applicant", columnList = "applicant_username"),
        @Index(name = "idx_ticket_technician", columnList = "technician_username")
})
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    /**
     * User who created the issue (Applicant/Requester).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_username", referencedColumnName = "username", nullable = false)
    private UserEntity applicant;

    /**
     * Technician assigned to handle the issue.
     * Can be null while ticket remains in OPEN status.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_username", referencedColumnName = "username")
    private UserEntity technician;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // [Nueva Funcionalidad: U-04/T-18/U-26/U-28 - Trazabilidad de SLA, catálogo, urgencia y concurrencia] - Soporta seguimiento de tiempos límites, catálogo de servicios y control de versiones - Conecta con: SlaPolicy, ServiceCatalogItem y TicketLifecycleService
    @Column(name = "service_catalog_id")
    private Long serviceCatalogId;

    @Column(name = "sla_due_date")
    private LocalDateTime slaDueDate;

    @Column(name = "first_response_due_date")
    private LocalDateTime firstResponseDueDate;

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "reopened_count", nullable = false)
    @Builder.Default
    private Integer reopenedCount = 0;

    @Column(name = "is_escalated", nullable = false)
    @Builder.Default
    private Boolean isEscalated = false;

    @Column(name = "urgent_reason", length = 500)
    private String urgentReason;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "resolution_summary", columnDefinition = "TEXT")
    private String resolutionSummary;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
