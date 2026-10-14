package com.roles.usermanagement.modules.dashboard.entity;

import com.roles.usermanagement.modules.dashboard.enums.MetricPeriod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Caches a dashboard metric for a given period and query scope (RF-07).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dashboard_metrics_cache", uniqueConstraints = {
        @UniqueConstraint(name = "uk_metric_scope", columnNames = {"metric_key", "period", "target_username"})
})
public class DashboardCacheMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_key", nullable = false, length = 60)
    private String metricKey; // e.g. "TOTAL_TICKETS", "OPEN_TICKETS", "RESOLVED_TICKETS"

    @Column(name = "metric_value", nullable = false)
    private Long metricValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetricPeriod period;

    @Column(name = "target_username", length = 50)
    private String targetUsername; // Null for global ADMIN metrics

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    /** Keeps the update timestamp current when the metric is created or modified. */
    @PrePersist
    @PreUpdate
    protected void onPersist() {
        this.lastUpdated = LocalDateTime.now();
    }

}
