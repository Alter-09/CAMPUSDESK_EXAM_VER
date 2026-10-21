package com.roles.usermanagement.modules.servicecatalog.repository;

import com.roles.usermanagement.modules.servicecatalog.entity.ServiceCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: U-26/A-13 - Repositorio de catálogo de servicios] - Consulta servicios activos ordenados por categoría y visualización - Conecta con: ServiceCatalogItem y ServiceCatalogService
public interface ServiceCatalogItemRepository extends JpaRepository<ServiceCatalogItem, Long> {
    List<ServiceCatalogItem> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<ServiceCatalogItem> findByCategoryAndIsActiveTrue(String category);
}
