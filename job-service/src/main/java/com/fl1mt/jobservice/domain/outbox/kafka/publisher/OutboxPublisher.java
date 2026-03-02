package com.fl1mt.jobservice.domain.outbox.kafka.publisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fl1mt.events.JobCreatedEvent;
import com.fl1mt.jobservice.domain.outbox.JobOutboxEvent;
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
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {

        List<JobOutboxEvent> events =
                outboxEventJpaRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);

        sendKafkaEvents(events);
    }

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void retryFailedEvents() {

        List<JobOutboxEvent> events =
                outboxEventJpaRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED);

        sendKafkaEvents(events);
    }

    private void sendKafkaEvents(List<JobOutboxEvent> events) {

        for (JobOutboxEvent event : events) {
            try {
                JobCreatedEvent kafkaEvent = objectMapper.readValue(event.getPayload(), JobCreatedEvent.class);

                kafkaTemplate
                        .send("job-created-topic", kafkaEvent)
                        .get();

                event.setStatus(OutboxStatus.SENT);
                log.info("JOB SERVICE. JobCreated kafka event sent to worker-service: {}", kafkaEvent);

            } catch (Exception e) {
                event.setStatus(OutboxStatus.FAILED);
                log.error("JOB SERVICE. Failed to send JobCreated kafka event to worker-service: {}", e);
            }
        }
    }
}
