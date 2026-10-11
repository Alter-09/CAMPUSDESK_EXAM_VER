package com.roles.usermanagement.modules.ticket.service;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.dto.TicketCreateRequest;
import com.roles.usermanagement.modules.ticket.dto.TicketResponse;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.mapper.TicketMapper;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import com.roles.usermanagement.persistance.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserCrudRepository userRepository;
    private final TicketMapper mapper;

    public TicketService(TicketRepository ticketRepository, UserCrudRepository userRepository, TicketMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public TicketResponse create(TicketCreateRequest request, Authentication auth){
        UserEntity aplicant = userRepository.findById(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        Ticket ticket = Ticket.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .category(request.category())
                .priority(request.priority())
                .status(TicketStatus.OPEN)
                .applicant(aplicant)
                .build();

        return mapper.toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse getById(Long id, Authentication auth){
        UserEntity applicant = userRepository.findById(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        auth.getAuthorities()
    }
}
