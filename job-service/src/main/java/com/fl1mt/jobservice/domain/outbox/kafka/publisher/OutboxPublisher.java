package com.fl1mt.jobservice.domain.outbox.kafka.publisher;
import com.fl1mt.events.JobCreatedEvent;
import com.fl1mt.jobservice.domain.outbox.OutboxEvent;
import com.fl1mt.jobservice.domain.outbox.OutboxEventJpaRepository;
import com.fl1mt.jobservice.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void publishEvents(){
        List<OutboxEvent> events = outboxEventJpaRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);

        for(OutboxEvent event : events){
            JobCreatedEvent kafkaEvent = new JobCreatedEvent(event.getAggregateId(), event.getPayload());
            kafkaTemplate.send(
                    "job-created-topic",
                    kafkaEvent
            );

            event.setStatus(OutboxStatus.SENT);

            log.info("Sending event to kafka: {}", kafkaEvent);
        }
    }
}
