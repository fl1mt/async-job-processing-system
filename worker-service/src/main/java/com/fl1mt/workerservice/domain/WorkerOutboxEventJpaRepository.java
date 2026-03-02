package com.fl1mt.workerservice.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface WorkerOutboxEventJpaRepository extends JpaRepository<WorkerOutboxEvent, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<WorkerOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus outboxStatus);
}
