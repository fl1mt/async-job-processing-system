package com.fl1mt.workerservice.api.kafka.consumer;
import com.fl1mt.events.JobCreatedEvent;
import com.fl1mt.workerservice.api.kafka.publisher.JobStartedPublisher;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventConsumer {

    private final JobStartedPublisher jobStartedPublisher;
    @Transactional
    @KafkaListener(
            topics = "job-created-topic",
            groupId = "worker-service"
    )
    public void consume(JobCreatedEvent event){
        log.info("Received event to kafka worker service: {}", event);
        jobStartedPublisher.publishJobStartedEvent(event.jobId());
    }
}
