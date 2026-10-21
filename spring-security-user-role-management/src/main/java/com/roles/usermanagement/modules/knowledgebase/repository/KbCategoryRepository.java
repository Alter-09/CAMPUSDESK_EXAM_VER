package com.roles.usermanagement.modules.knowledgebase.repository;

import com.roles.usermanagement.modules.knowledgebase.entity.KbCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: A-04/U-08 - Repositorio de categorías KB] - Consulta categorías activas ordenadas para autoservicio y administración - Conecta con: KbCategory y KnowledgeBaseService
public interface KbCategoryRepository extends JpaRepository<KbCategory, Long> {
    List<KbCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
}
