package com.roles.usermanagement.modules.lifecycle.repository;

import com.roles.usermanagement.modules.lifecycle.entity.LifecycleRule;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LifecycleRuleRepository extends JpaRepository<LifecycleRule, Long> {

    // Finds the rule that allows executing an action from a specific status.
    // Optional.empty() means that transition is NOT permitted.
    Optional<LifecycleRule> findByCurrentStatusAndAction(TicketStatus currentStatus, TransitionAction action);
}