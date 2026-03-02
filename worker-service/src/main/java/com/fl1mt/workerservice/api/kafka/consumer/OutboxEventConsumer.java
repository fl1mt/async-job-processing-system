package com.fl1mt.workerservice.api.kafka.consumer;
import com.fl1mt.events.JobCreatedEvent;
import com.fl1mt.workerservice.api.kafka.publisher.JobStartedPublisher;
import com.fl1mt.workerservice.domain.JobProcessor;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventConsumer {

    private final JobStartedPublisher jobStartedPublisher;
    private final ExecutorService executorService;
    private final JobProcessor jobProcessor;
    @Transactional
    @KafkaListener(
            topics = "job-created-topic",
            groupId = "worker-service"
    )
    public void consume(JobCreatedEvent event) {
        log.info("WORKER SERVICE. Received JobCreated kafka event from job-service: {}", event);
        jobStartedPublisher.publishJobStartedEvent(event.jobId());
        executorService.submit(() -> {
            try{
                jobProcessor.process(event);
            } catch (Exception e){
                // job failed publisher
                log.info("WORKER SERVICE. Failed to complete job. Job id: " + event.jobId() +
                        "\n" + e);
            }
        });

    }
}
