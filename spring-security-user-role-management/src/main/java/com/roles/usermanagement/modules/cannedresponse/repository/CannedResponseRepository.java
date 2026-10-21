package com.roles.usermanagement.modules.cannedresponse.repository;

import com.roles.usermanagement.modules.cannedresponse.entity.CannedResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: T-04/A-19 - Repositorio de respuestas rápidas] - Consulta de plantillas globales o creadas por un técnico específico - Conecta con: CannedResponse y CannedResponseService
public interface CannedResponseRepository extends JpaRepository<CannedResponse, Long> {
    List<CannedResponse> findByIsGlobalTrueOrCreatedByUsername(String createdByUsername);
}
