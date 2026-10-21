package com.roles.usermanagement.modules.servicecatalog.controller;

import com.roles.usermanagement.modules.servicecatalog.dto.CatalogItemRequest;
import com.roles.usermanagement.modules.servicecatalog.dto.CatalogItemResponse;
import com.roles.usermanagement.modules.servicecatalog.service.ServiceCatalogService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [Nueva Funcionalidad: U-26/A-13 - Controlador REST de catálogo de servicios] - Endpoints para explorar y administrar servicios IT solicitables - Conecta con: ServiceCatalogService
@RestController
@RequestMapping("/api/v1/service-catalog")
@RequiredArgsConstructor
@Tag(name = "Service Catalog", description = "Catálogo de servicios de TI solicitables por el campus")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ServiceCatalogController {

    private final ServiceCatalogService catalogService;

    @GetMapping("/items")
    @Operation(summary = "Consultar catálogo completo de servicios IT activos (MOD-25, U-26)")
    public ResponseEntity<List<CatalogItemResponse>> getActiveItems() {
        return ResponseEntity.ok(catalogService.getActiveItems());
    }

    @GetMapping("/items/category/{category}")
    @Operation(summary = "Filtrar servicios solicitables por categoría (MOD-25, U-27)")
    public ResponseEntity<List<CatalogItemResponse>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(catalogService.getItemsByCategory(category));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo ítem o servicio en el catálogo (ADMIN) (MOD-26, A-13)")
    public ResponseEntity<CatalogItemResponse> createItem(@Valid @RequestBody CatalogItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createItem(request));
    }
}
