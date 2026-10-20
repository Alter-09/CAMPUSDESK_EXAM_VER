package com.roles.usermanagement.modules.asset.controller;

import com.roles.usermanagement.modules.asset.dto.AssetRequest;
import com.roles.usermanagement.modules.asset.dto.AssetResponse;
import com.roles.usermanagement.modules.asset.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// [Nueva Funcionalidad: T-23/A-27 - Controlador REST de inventario de activos] - Endpoints para consulta y alta de equipos informáticos - Conecta con: AssetService
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/user/{username}")
    public ResponseEntity<List<AssetResponse>> getByUser(@PathVariable String username) {
        return ResponseEntity.ok(assetService.getAssetsByUser(username));
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<AssetResponse>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(assetService.getAssetsByTicket(ticketId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.createAsset(request));
    }

    @PostMapping("/ticket/{ticketId}/link/{assetId}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    public ResponseEntity<Void> linkToTicket(@PathVariable Long ticketId, @PathVariable Long assetId) {
        assetService.linkAssetToTicket(ticketId, assetId);
        return ResponseEntity.noContent().build();
    }
}
