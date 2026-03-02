package com.fl1mt.jobservice.domain.outbox;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<JobOutboxEvent, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<JobOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus outboxStatus);
}
