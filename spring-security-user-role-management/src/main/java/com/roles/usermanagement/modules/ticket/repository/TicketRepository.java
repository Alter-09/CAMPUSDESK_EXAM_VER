package com.roles.usermanagement.modules.ticket.repository;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// JpaRepository -> CRUD // JpaSpecification -> queries with programmatic filters
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findForUpdate(@Param("id") Long id);

    // [Nueva Funcionalidad: T-18/A-08 - Consulta de tickets con riesgo de vencer SLA] - Detecta tickets activos cuya fecha límite vence antes de un umbral - Conecta con: SlaService
    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN (com.roles.usermanagement.modules.lifecycle.enums.TicketStatus.RESOLVED, com.roles.usermanagement.modules.lifecycle.enums.TicketStatus.CLOSED, com.roles.usermanagement.modules.lifecycle.enums.TicketStatus.CANCELLED) AND t.slaDueDate IS NOT NULL AND t.slaDueDate <= :threshold")
    java.util.List<Ticket> findTicketsNearingSlaBreach(@Param("threshold") java.time.LocalDateTime threshold);

    // [Nueva Funcionalidad: U-03/U-21 - Listado paginado de tickets de un solicitante] - Soporta paginación eficiente en portal de usuario - Conecta con: TicketService
    org.springframework.data.domain.Page<Ticket> findByApplicant_Username(String username, org.springframework.data.domain.Pageable pageable);

    // [Nueva Funcionalidad: A-25 - Consulta de cola global de tickets sin asignar] - Obtiene tickets en estado OPEN sin técnico asignado - Conecta con: TicketAssignmentService
    @Query("SELECT t FROM Ticket t WHERE t.status = com.roles.usermanagement.modules.lifecycle.enums.TicketStatus.OPEN AND t.technician IS NULL ORDER BY t.createdAt ASC")
    org.springframework.data.domain.Page<Ticket> findUnassignedTickets(org.springframework.data.domain.Pageable pageable);

    // [Ejercicio 1 Examen: Consultas JPQL con filtros por etiquetas respetando visibilidad de rol]
    @Query("SELECT DISTINCT t FROM Ticket t JOIN t.tags tag WHERE UPPER(tag.nombre) = UPPER(:nombre) ORDER BY t.createdAt DESC")
    java.util.List<Ticket> findByTagNombre(@Param("nombre") String nombre);

    @Query("SELECT DISTINCT t FROM Ticket t JOIN t.tags tag WHERE UPPER(tag.nombre) = UPPER(:nombre) AND t.technician.username = :username ORDER BY t.createdAt DESC")
    java.util.List<Ticket> findByTagNombreAndTechnician(@Param("nombre") String nombre, @Param("username") String username);

    @Query("SELECT DISTINCT t FROM Ticket t JOIN t.tags tag WHERE UPPER(tag.nombre) = UPPER(:nombre) AND t.applicant.username = :username ORDER BY t.createdAt DESC")
    java.util.List<Ticket> findByTagNombreAndApplicant(@Param("nombre") String nombre, @Param("username") String username);

    // [Ejercicio 2 Examen: Consulta de tickets con reaperturas para ADMIN]
    java.util.List<Ticket> findByReopenedCountGreaterThanOrderByUpdatedAtDesc(Integer count);
}
