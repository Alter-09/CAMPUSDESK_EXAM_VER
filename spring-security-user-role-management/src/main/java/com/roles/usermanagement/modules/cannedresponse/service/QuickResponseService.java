package com.roles.usermanagement.modules.cannedresponse.service;

import com.roles.usermanagement.modules.cannedresponse.dto.QuickResponseRequest;
import com.roles.usermanagement.modules.cannedresponse.dto.QuickResponseResponse;
import com.roles.usermanagement.modules.cannedresponse.entity.QuickResponse;
import com.roles.usermanagement.modules.cannedresponse.repository.QuickResponseRepository;
import com.roles.usermanagement.modules.comment.dto.CommentResponse;
import com.roles.usermanagement.modules.comment.entity.Comment;
import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import com.roles.usermanagement.modules.comment.repository.CommentRepository;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import com.roles.usermanagement.persistance.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Servicio para plantillas de solución rápida y aplicación como comentarios (Ejercicio 5 Examen).
 */
@Service
@RequiredArgsConstructor
public class QuickResponseService {

    private final QuickResponseRepository quickResponseRepository;
    private final TicketRepository ticketRepository;
    private final UserCrudRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public QuickResponseResponse createPlantilla(QuickResponseRequest request, String technicianUsername) {
        if (quickResponseRepository.existsByTechnician_UsernameAndTituloIgnoreCase(technicianUsername, request.titulo().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya posees una plantilla registrada con el título '" + request.titulo().trim() + "'");
        }

        UserEntity technician = userRepository.findById(technicianUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));

        QuickResponse quickResponse = QuickResponse.builder()
                .titulo(request.titulo().trim())
                .contenido(request.contenido().trim())
                .categoria(request.categoria())
                .technician(technician)
                .build();

        QuickResponse saved = quickResponseRepository.save(quickResponse);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<QuickResponseResponse> getMisPlantillas(String technicianUsername) {
        return quickResponseRepository.findByTechnician_UsernameOrderByCreatedAtDesc(technicianUsername).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse aplicarPlantilla(Long ticketId, Long plantillaId, String technicianUsername) {
        // Regla 1: Validar que la plantilla pertenezca al técnico autenticado
        QuickResponse plantilla = quickResponseRepository.findByIdAndTechnician_Username(plantillaId, technicianUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "La plantilla solicitada no existe o no pertenece al técnico autenticado"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        // Regla 2: Validar que el técnico tenga asignado dicho ticket
        if (ticket.getTechnician() == null || !ticket.getTechnician().getUsername().equals(technicianUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el técnico asignado a la incidencia puede aplicarle una plantilla de respuesta");
        }

        // Regla 3: Validar que el ticket esté en estado EN_PROCESO
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden aplicar plantillas en tickets en estado EN_PROCESO (IN_PROGRESS)");
        }

        UserEntity author = userRepository.findById(technicianUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .author(author)
                .content(plantilla.getContenido())
                .visibility(CommentVisibility.PUBLIC)
                .isInternal(false)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponse(
                savedComment.getId(),
                ticket.getId(),
                savedComment.getContent(),
                author.getUsername(),
                savedComment.getVisibility(),
                savedComment.getCreatedAt(),
                savedComment.getIsInternal(),
                savedComment.getEditedAt()
        );
    }

    private QuickResponseResponse toResponse(QuickResponse q) {
        return new QuickResponseResponse(
                q.getId(),
                q.getTitulo(),
                q.getContenido(),
                q.getCategoria(),
                q.getTechnician().getUsername(),
                q.getCreatedAt()
        );
    }
}
