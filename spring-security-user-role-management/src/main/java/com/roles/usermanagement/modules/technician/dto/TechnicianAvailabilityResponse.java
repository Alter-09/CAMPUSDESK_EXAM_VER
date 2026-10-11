package com.roles.usermanagement.modules.technician.dto;

/**
 * Datos mínimos de un técnico disponible, para elegirlo al asignar un ticket.
 * Incluye solo lo necesario para decidir: quién es, en qué se especializa y qué tan cargado está.
 */
public record TechnicianAvailabilityResponse(
        String username,
        String specialty,
        Integer activeTicketsCount
) {
}