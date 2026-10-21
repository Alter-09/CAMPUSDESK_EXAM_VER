package com.roles.usermanagement.modules.asset.service;

import com.roles.usermanagement.modules.asset.dto.AssetRequest;
import com.roles.usermanagement.modules.asset.dto.AssetResponse;
import com.roles.usermanagement.modules.asset.entity.Asset;
import com.roles.usermanagement.modules.asset.entity.TicketAssetLink;
import com.roles.usermanagement.modules.asset.enums.AssetStatus;
import com.roles.usermanagement.modules.asset.repository.AssetRepository;
import com.roles.usermanagement.modules.asset.repository.TicketAssetLinkRepository;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: T-23/A-27 - Servicio de activos IT] - Inventario de equipos y asociación a tickets de soporte técnico - Conecta con: AssetRepository, TicketAssetLinkRepository y TicketRepository
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final TicketAssetLinkRepository assetLinkRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssetsByUser(String username) {
        return assetRepository.findByAssignedToUsername(username).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AssetResponse createAsset(AssetRequest request) {
        Asset asset = Asset.builder()
                .name(request.name())
                .serialNumber(request.serialNumber())
                .category(request.category())
                .status(request.status() != null ? request.status() : AssetStatus.IN_USE)
                .assignedToUsername(request.assignedToUsername())
                .location(request.location())
                .build();

        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public void linkAssetToTicket(Long ticketId, Long assetId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Activo no encontrado"));

        TicketAssetLink link = TicketAssetLink.builder()
                .ticket(ticket)
                .asset(asset)
                .build();
        assetLinkRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssetsByTicket(Long ticketId) {
        return assetLinkRepository.findByTicketId(ticketId).stream()
                .map(link -> toResponse(link.getAsset()))
                .toList();
    }

    private AssetResponse toResponse(Asset a) {
        return new AssetResponse(
                a.getId(),
                a.getName(),
                a.getSerialNumber(),
                a.getCategory(),
                a.getStatus(),
                a.getAssignedToUsername(),
                a.getLocation(),
                a.getCreatedAt()
        );
    }
}
