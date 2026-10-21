package com.roles.usermanagement.modules.tag.service;

import com.roles.usermanagement.modules.tag.dto.TagRequest;
import com.roles.usermanagement.modules.tag.dto.TagResponse;
import com.roles.usermanagement.modules.tag.entity.TicketTag;
import com.roles.usermanagement.modules.tag.entity.TicketTagAssignment;
import com.roles.usermanagement.modules.tag.repository.TicketTagAssignmentRepository;
import com.roles.usermanagement.modules.tag.repository.TicketTagRepository;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: U-22/A-23 - Servicio de etiquetas] - Gestión de catálogo de tags y vinculación directa con tickets - Conecta con: TicketTagRepository, TicketTagAssignmentRepository y TicketRepository
@Service
@RequiredArgsConstructor
public class TicketTagService {

    private final TicketTagRepository tagRepository;
    private final TicketTagAssignmentRepository assignmentRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getColor()))
                .toList();
    }

    @Transactional
    public TagResponse createTag(TagRequest request) {
        TicketTag tag = tagRepository.findByNameIgnoreCase(request.name().trim())
                .orElseGet(() -> tagRepository.save(TicketTag.builder()
                        .name(request.name().trim())
                        .color(request.color() != null ? request.color() : "#3b82f6")
                        .build()));
        return new TagResponse(tag.getId(), tag.getName(), tag.getColor());
    }

    @Transactional
    public void assignTagToTicket(Long ticketId, Long tagId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        TicketTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag no encontrado"));

        TicketTagAssignment assignment = TicketTagAssignment.builder()
                .ticket(ticket)
                .tag(tag)
                .build();
        assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTagsForTicket(Long ticketId) {
        return assignmentRepository.findByTicketId(ticketId).stream()
                .map(a -> new TagResponse(a.getTag().getId(), a.getTag().getName(), a.getTag().getColor()))
                .toList();
    }
}
