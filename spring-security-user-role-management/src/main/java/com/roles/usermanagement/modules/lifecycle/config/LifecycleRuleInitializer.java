package com.roles.usermanagement.modules.lifecycle.config;

import com.roles.usermanagement.modules.lifecycle.entity.LifecycleRule;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;
import com.roles.usermanagement.modules.lifecycle.repository.LifecycleRuleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Clase de configuración: Spring la lee al arrancar y registra los beans que define
@Configuration
public class LifecycleRuleInitializer {

    // ApplicationRunner se ejecuta UNA vez, justo después de que la aplicación termina de arrancar
    @Bean
    public ApplicationRunner initializeLifecycleRules(LifecycleRuleRepository repository) {
        return args -> {
            // Las 5 reglas del flujo ABIERTA → ASIGNADA → EN_PROCESO → RESUELTA → CERRADA
            List<LifecycleRule> rules = List.of(
                    // Solo el administrador asigna un técnico
                    rule(TicketStatus.OPEN, TicketStatus.ASSIGNED, TransitionAction.ASSIGN,
                            "ROLE_ADMIN", false, false),
                    // Solo el administrador reasigna (el ticket sigue en ASSIGNED)
                    rule(TicketStatus.ASSIGNED, TicketStatus.ASSIGNED, TransitionAction.REASSIGN,
                            "ROLE_ADMIN", false, false),
                    // El técnico responsable inicia la atención
                    rule(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TransitionAction.START_PROGRESS,
                            "ROLE_TECHNICIAN", false, true),
                    // El técnico responsable marca como resuelto (solo desde IN_PROGRESS)
                    rule(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TransitionAction.RESOLVE,
                            "ROLE_TECHNICIAN", false, true),
                    // Solo el solicitante confirma el cierre
                    rule(TicketStatus.RESOLVED, TicketStatus.CLOSED, TransitionAction.CONFIRM_CLOSE,
                            "ROLE_USER", true, false)
            );

            // Solo se guardan las reglas que aún no existen, así el arranque es idempotente
            for (LifecycleRule rule : rules) {
                boolean exists = repository
                        .findByCurrentStatusAndAction(rule.getCurrentStatus(), rule.getAction())
                        .isPresent();
                if (!exists) {
                    repository.save(rule);
                }
            }
        };
    }

    // Método auxiliar para no repetir el builder cinco veces
    private LifecycleRule rule(TicketStatus current, TicketStatus target, TransitionAction action,
                               String requiredRole, boolean mustBeApplicant, boolean mustBeAssignedTechnician) {
        return LifecycleRule.builder()
                .currentStatus(current)
                .targetStatus(target)
                .action(action)
                .requiredRole(requiredRole)
                .mustBeApplicant(mustBeApplicant)
                .mustBeAssignedTechnician(mustBeAssignedTechnician)
                .build();
    }
}