package com.fl1mt.workerservice.api.kafka.publisher;

import com.fl1mt.events.JobFailedEvent;
import com.fl1mt.events.JobStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobFailedPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishJobFailedEvent(Long jobId){
        JobFailedEvent jobFailedEvent = new JobFailedEvent(jobId);
        log.info("WORKER SERVICE. Sending JobFailed kafka event to job-service. Job ID: " + jobFailedEvent.jobId());

        try {
            kafkaTemplate.send(
                    "job-failed-topic", jobFailedEvent).get();
        } catch (Exception e) {
            log.error("WORKER SERVICE. Failed to send JobFailed kafka event to job-service: {}", e);
        }
    }
}
