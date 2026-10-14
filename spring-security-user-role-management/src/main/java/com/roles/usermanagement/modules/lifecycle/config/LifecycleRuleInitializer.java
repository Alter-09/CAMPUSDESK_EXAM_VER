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

// Configuration class: Spring reads it on startup and registers the beans it defines
@Configuration
public class LifecycleRuleInitializer {

    // ApplicationRunner runs ONCE, right after the application finishes starting up
    @Bean
    public ApplicationRunner initializeLifecycleRules(LifecycleRuleRepository repository) {
        return args -> {
            // The 5 rules of the workflow OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
            List<LifecycleRule> rules = List.of(
                    // Only the administrator assigns a technician
                    rule(TicketStatus.OPEN, TicketStatus.ASSIGNED, TransitionAction.ASSIGN,
                            "ROLE_ADMIN", false, false),
                    // Only the administrator reassigns (ticket remains ASSIGNED)
                    rule(TicketStatus.ASSIGNED, TicketStatus.ASSIGNED, TransitionAction.REASSIGN,
                            "ROLE_ADMIN", false, false),
                    // Responsible technician starts progress
                    rule(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TransitionAction.START_PROGRESS,
                            "ROLE_TECHNICIAN", false, true),
                    // Responsible technician marks as resolved (only from IN_PROGRESS)
                    rule(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TransitionAction.RESOLVE,
                            "ROLE_TECHNICIAN", false, true),
                    // Only the requester confirms closure
                    rule(TicketStatus.RESOLVED, TicketStatus.CLOSED, TransitionAction.CONFIRM_CLOSE,
                            "ROLE_USER", true, false)
            );

            // Only rules that do not exist yet are saved, ensuring idempotent startup
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

    // Helper method to avoid repeating the builder five times
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