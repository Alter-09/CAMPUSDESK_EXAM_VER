package com.roles.usermanagement.modules.asset.repository;

import com.roles.usermanagement.modules.asset.entity.TicketAssetLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: T-23 - Repositorio de vinculación de activos a tickets] - Consulta equipos asociados a un ticket de soporte - Conecta con: TicketAssetLink y AssetService
public interface TicketAssetLinkRepository extends JpaRepository<TicketAssetLink, Long> {
    List<TicketAssetLink> findByTicketId(Long ticketId);
}
