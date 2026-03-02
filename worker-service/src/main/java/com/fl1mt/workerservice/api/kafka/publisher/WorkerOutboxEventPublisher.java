package com.fl1mt.workerservice.api.kafka.publisher;
import com.fl1mt.events.JobCompletedEvent;
import com.fl1mt.workerservice.domain.OutboxStatus;
import com.fl1mt.workerservice.domain.WorkerOutboxEvent;
import com.fl1mt.workerservice.domain.WorkerOutboxEventJpaRepository;
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
public class WorkerOutboxEventPublisher {
    private final WorkerOutboxEventJpaRepository workerOutboxEventJpaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {

        List<WorkerOutboxEvent> events =
                workerOutboxEventJpaRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);
        sendKafkaEvents(events);
    }

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void retryFailedEvents() {

        List<WorkerOutboxEvent> events =
                workerOutboxEventJpaRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED);
        sendKafkaEvents(events);
    }

    private void sendKafkaEvents(List<WorkerOutboxEvent> events) {

        for (WorkerOutboxEvent event : events) {
            try {
                JobCompletedEvent jobCompletedEvent = new JobCompletedEvent(event.getAggregateId(), event.getResult());
                kafkaTemplate
                        .send("job-completed-topic", jobCompletedEvent)
                        .get();

                event.setStatus(OutboxStatus.SENT);
                log.info("WORKER SERVICE. JobCompleted kafka event sent to job-service. Job id: " + jobCompletedEvent.jobId() +
                        " Result: " + jobCompletedEvent.result());

            } catch (Exception e) {
                event.setStatus(OutboxStatus.FAILED);
                log.error("WORKER SERVICE. Failed to send JobCompleted kafka event to job-service: {}", e);
            }
        }
    }
}
