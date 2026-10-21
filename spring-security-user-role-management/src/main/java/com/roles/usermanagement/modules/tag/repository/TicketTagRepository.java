package com.roles.usermanagement.modules.tag.repository;

import com.roles.usermanagement.modules.tag.entity.TicketTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// [Nueva Funcionalidad: U-22/A-23 - Repositorio de tags] - Catálogo de etiquetas disponibles en el sistema - Conecta con: TicketTag y TicketTagService
public interface TicketTagRepository extends JpaRepository<TicketTag, Long> {
    Optional<TicketTag> findByNameIgnoreCase(String name);
}
