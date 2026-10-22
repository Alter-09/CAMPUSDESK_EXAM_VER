package com.roles.usermanagement.modules.sla.controller;

import com.roles.usermanagement.modules.sla.dto.SlaPolicyRequest;
import com.roles.usermanagement.modules.sla.dto.SlaPolicyResponse;
import com.roles.usermanagement.modules.sla.entity.SlaPolicy;
import com.roles.usermanagement.modules.sla.repository.SlaPolicyRepository;
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

@RestController
@RequestMapping("/api/v1/sla")
@RequiredArgsConstructor
@Tag(name = "SLA Policies", description = "Políticas de Acuerdos de Nivel de Servicio (SLA) y cumplimiento")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class SlaController {

    private final SlaPolicyRepository policyRepository;

    @GetMapping("/policies")
    @Operation(summary = "Consultar políticas activas de SLA por prioridad ")
    public ResponseEntity<List<SlaPolicyResponse>> getAllPolicies() {
        List<SlaPolicyResponse> list = policyRepository.findAll().stream()
                .map(p -> new SlaPolicyResponse(p.getId(), p.getName(), p.getPriority(), p.getFirstResponseHours(), p.getResolutionHours(), p.getIsActive(), p.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/policies")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Configurar o actualizar reglas y tiempos de política SLA (ADMIN)")
    public ResponseEntity<SlaPolicyResponse> createOrUpdatePolicy(@Valid @RequestBody SlaPolicyRequest request) {
        SlaPolicy policy = policyRepository.findByPriorityAndIsActiveTrue(request.priority())
                .orElse(SlaPolicy.builder().priority(request.priority()).build());

        policy.setName(request.name());
        policy.setFirstResponseHours(request.firstResponseHours());
        policy.setResolutionHours(request.resolutionHours());
        policy.setIsActive(request.isActive() == null || request.isActive());

        SlaPolicy saved = policyRepository.save(policy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SlaPolicyResponse(saved.getId(), saved.getName(), saved.getPriority(), saved.getFirstResponseHours(), saved.getResolutionHours(), saved.getIsActive(), saved.getCreatedAt()));
    }
}
