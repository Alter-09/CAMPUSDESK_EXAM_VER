package com.roles.usermanagement.modules.statushistory.repository;

import com.roles.usermanagement.modules.statushistory.entity.StatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for ticket status history.
 */
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    /**
     * Returns ticket history in chronological order
     * (from oldest to most recent change).
     */
    List<StatusHistory> findByTicketIdOrderByChangedAtAscIdAsc(Long ticketId);
}