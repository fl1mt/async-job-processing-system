package com.fl1mt.jobservice.kafka.consumer;

import com.fl1mt.events.JobCompletedEvent;
import com.fl1mt.events.JobStartedEvent;
import com.fl1mt.jobservice.domain.Job;
import com.fl1mt.jobservice.domain.JobJpaRepository;
import com.fl1mt.jobservice.domain.JobStatus;
import com.fl1mt.jobservice.redis.JobStatusCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobCompletedConsumer {
    private final JobJpaRepository jobJpaRepository;
    private final JobStatusCacheService jobStatusCacheService;

    @KafkaListener(
            topics = "job-completed-topic",
            groupId = "job-service"
    )
    @Transactional
    public void consume(JobCompletedEvent event){
        Job job = jobJpaRepository.findById(event.jobId())
                .orElseThrow(() -> new RuntimeException("Job not found!"));

        job.setStatus(JobStatus.COMPLETED);
        job.setResult(event.result());
        jobStatusCacheService.setStatus(job.getId(), job.getStatus().name());
        log.info("Redis. Set status in cache from JobCompleted Consumer.");
        log.info("JOB SERVICE. Received JobCompleted kafka event from worker-service. Job ID: "+ job.getId() + " with status: " + job.getStatus()
        + " Result: " + job.getResult());
    }
}
