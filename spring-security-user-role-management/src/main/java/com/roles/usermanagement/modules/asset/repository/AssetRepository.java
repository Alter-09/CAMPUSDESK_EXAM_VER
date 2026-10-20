package com.roles.usermanagement.modules.asset.repository;

import com.roles.usermanagement.modules.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// [Nueva Funcionalidad: T-23/A-27 - Repositorio de activos IT] - Búsqueda de equipos por usuario asignado o número de serie - Conecta con: Asset y AssetService
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByAssignedToUsername(String assignedToUsername);
    Optional<Asset> findBySerialNumber(String serialNumber);
}
