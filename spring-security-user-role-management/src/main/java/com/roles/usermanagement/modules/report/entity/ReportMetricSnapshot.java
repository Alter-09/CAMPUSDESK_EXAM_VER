package com.roles.usermanagement.modules.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: A-07/A-08/A-10 - Snapshots de métricas para reportes ejecutivos] - Almacena consolidados diarios/mensuales de SLA, volumen y satisfacción - Conecta con: ReportService y DashboardService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "report_metric_snapshots", indexes = {
        @Index(name = "idx_snapshot_date_metric", columnList = "snapshot_date, metric_name")
})
public class ReportMetricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "metric_value", nullable = false)
    private Double metricValue;

    @Column(length = 50)
    private String dimension; // CATEGORY, TECHNICIAN, PRIORITY

    @Column(name = "dimension_value", length = 100)
    private String dimensionValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
