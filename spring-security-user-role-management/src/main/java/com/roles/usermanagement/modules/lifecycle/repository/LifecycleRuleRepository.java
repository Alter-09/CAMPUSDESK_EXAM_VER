package com.roles.usermanagement.modules.lifecycle.repository;

import com.roles.usermanagement.modules.lifecycle.entity.LifecycleRule;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LifecycleRuleRepository extends JpaRepository<LifecycleRule, Long> {

    // Busca la regla que permite ejecutar una acción desde un estado concreto.
    // Optional.empty() significa que esa transición NO está permitida.
    Optional<LifecycleRule> findByCurrentStatusAndAction(TicketStatus currentStatus, TransitionAction action);
}