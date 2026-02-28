package com.fl1mt.jobservice.domain.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {
}
