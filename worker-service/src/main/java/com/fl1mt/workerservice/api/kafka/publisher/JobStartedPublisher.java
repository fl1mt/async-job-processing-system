package com.fl1mt.workerservice.api.kafka.publisher;
import com.fl1mt.events.JobStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobStartedPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishJobStartedEvent(Long jobId){
        JobStartedEvent jobStartedEvent = new JobStartedEvent(jobId);
        log.info("WORKER SERVICE. Sending JobStarted kafka event to job-service. Job ID: " + jobStartedEvent.jobId());

        kafkaTemplate.send(
                "job-started-topic",
                jobStartedEvent
        );
    }

}
