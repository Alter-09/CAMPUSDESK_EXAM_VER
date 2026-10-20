package com.roles.usermanagement.modules.servicecatalog.service;

import com.roles.usermanagement.modules.servicecatalog.dto.CatalogItemRequest;
import com.roles.usermanagement.modules.servicecatalog.dto.CatalogItemResponse;
import com.roles.usermanagement.modules.servicecatalog.entity.ServiceCatalogItem;
import com.roles.usermanagement.modules.servicecatalog.repository.ServiceCatalogItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: U-26/A-13 - Servicio de catálogo de servicios IT] - Consulta y administración de ítems de autoservicio - Conecta con: ServiceCatalogItemRepository
@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceCatalogItemRepository catalogRepository;

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> getActiveItems() {
        return catalogRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> getItemsByCategory(String category) {
        return catalogRepository.findByCategoryAndIsActiveTrue(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CatalogItemResponse createItem(CatalogItemRequest request) {
        ServiceCatalogItem item = ServiceCatalogItem.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .icon(request.icon())
                .estimatedHours(request.estimatedHours())
                .formSchema(request.formSchema())
                .isActive(request.isActive() == null || request.isActive())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();

        return toResponse(catalogRepository.save(item));
    }

    private CatalogItemResponse toResponse(ServiceCatalogItem i) {
        return new CatalogItemResponse(
                i.getId(),
                i.getName(),
                i.getDescription(),
                i.getCategory(),
                i.getIcon(),
                i.getEstimatedHours(),
                i.getFormSchema(),
                i.getIsActive(),
                i.getDisplayOrder(),
                i.getCreatedAt()
        );
    }
}
