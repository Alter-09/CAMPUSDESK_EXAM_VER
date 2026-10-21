package com.roles.usermanagement.modules.knowledgebase.repository;

import com.roles.usermanagement.modules.knowledgebase.entity.KbArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// [Nueva Funcionalidad: U-08/U-09/U-20 - Repositorio de artículos KB] - Búsqueda de artículos públicos por texto o categoría con paginación - Conecta con: KbArticle y KnowledgeBaseService
public interface KbArticleRepository extends JpaRepository<KbArticle, Long> {

    Page<KbArticle> findByIsPublishedTrue(Pageable pageable);

    Page<KbArticle> findByCategoryIdAndIsPublishedTrue(Long categoryId, Pageable pageable);

    @Query("SELECT a FROM KbArticle a WHERE a.isPublished = true AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<KbArticle> searchPublishedArticles(@Param("query") String query);

    @Query("SELECT a FROM KbArticle a WHERE a.isPublished = true AND LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY a.viewsCount DESC")
    List<KbArticle> suggestArticlesByTitle(@Param("title") String title, Pageable pageable);
}
