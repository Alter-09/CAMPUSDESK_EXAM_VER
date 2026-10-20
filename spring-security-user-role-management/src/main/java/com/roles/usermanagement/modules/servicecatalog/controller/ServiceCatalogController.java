package com.roles.usermanagement.modules.servicecatalog.controller;

import com.roles.usermanagement.modules.servicecatalog.dto.CatalogItemRequest;
import com.roles.usermanagement.modules.servicecatalog.dto.CatalogItemResponse;
import com.roles.usermanagement.modules.servicecatalog.service.ServiceCatalogService;
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
public class ServiceCatalogController {

    private final ServiceCatalogService catalogService;

    @GetMapping("/items")
    public ResponseEntity<List<CatalogItemResponse>> getActiveItems() {
        return ResponseEntity.ok(catalogService.getActiveItems());
    }

    @GetMapping("/items/category/{category}")
    public ResponseEntity<List<CatalogItemResponse>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(catalogService.getItemsByCategory(category));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatalogItemResponse> createItem(@Valid @RequestBody CatalogItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createItem(request));
    }
}
