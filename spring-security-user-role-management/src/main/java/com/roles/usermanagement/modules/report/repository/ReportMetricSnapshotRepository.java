package com.roles.usermanagement.modules.report.repository;

import com.roles.usermanagement.modules.report.entity.ReportMetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

// [Nueva Funcionalidad: A-07/A-10 - Repositorio de métricas consolidadas] - Consulta snapshots de métricas en rangos de fechas - Conecta con: ReportMetricSnapshot y ReportService
public interface ReportMetricSnapshotRepository extends JpaRepository<ReportMetricSnapshot, Long> {
    List<ReportMetricSnapshot> findBySnapshotDateBetween(LocalDate start, LocalDate end);
    List<ReportMetricSnapshot> findByMetricNameAndSnapshotDateBetween(String metricName, LocalDate start, LocalDate end);
}
