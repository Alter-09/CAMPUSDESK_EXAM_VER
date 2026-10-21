package com.roles.usermanagement.modules.asset.controller;

import com.roles.usermanagement.modules.asset.dto.AssetRequest;
import com.roles.usermanagement.modules.asset.dto.AssetResponse;
import com.roles.usermanagement.modules.asset.service.AssetService;
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

// [Nueva Funcionalidad: T-23/A-27 - Controlador REST de inventario de activos] - Endpoints para consulta y alta de equipos informáticos - Conecta con: AssetService
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Inventario de hardware, equipos y activos asignados")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/user/{username}")
    @Operation(summary = "Consultar inventario de activos asignados a un usuario específico (MOD-34, T-23)")
    public ResponseEntity<List<AssetResponse>> getByUser(@PathVariable String username) {
        return ResponseEntity.ok(assetService.getAssetsByUser(username));
    }

    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "Consultar activos y equipos vinculados a una incidencia (MOD-34)")
    public ResponseEntity<List<AssetResponse>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(assetService.getAssetsByTicket(ticketId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar nuevo activo o equipo en el inventario institucional (ADMIN) (MOD-34, A-27)")
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.createAsset(request));
    }

    @PostMapping("/ticket/{ticketId}/link/{assetId}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Vincular equipo o activo informático a un ticket de soporte (MOD-34)")
    public ResponseEntity<Void> linkToTicket(@PathVariable Long ticketId, @PathVariable Long assetId) {
        assetService.linkAssetToTicket(ticketId, assetId);
        return ResponseEntity.noContent().build();
    }
}
