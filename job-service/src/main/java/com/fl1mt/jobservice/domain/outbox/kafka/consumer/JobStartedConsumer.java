package com.fl1mt.jobservice.domain.outbox.kafka.consumer;


import com.fl1mt.events.JobStartedEvent;
import com.fl1mt.jobservice.domain.Job;
import com.fl1mt.jobservice.domain.JobJpaRepository;
import com.fl1mt.jobservice.domain.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobStartedConsumer {

    private final JobJpaRepository jobJpaRepository;

    @KafkaListener(
            topics = "job-started-topic",
            groupId = "job-service"
    )
    @Transactional
    public void consume(JobStartedEvent event){
        Job job = jobJpaRepository.findById(event.jobId())
                .orElseThrow(() -> new RuntimeException("Job not found!"));
        job.setStatus(JobStatus.IN_PROGRESS);
        log.info("JOB SERVICE. Received JobStarted kafka event from worker-service. Job ID: "+ job.getId() + " with status: " + job.getStatus());
    }
}
