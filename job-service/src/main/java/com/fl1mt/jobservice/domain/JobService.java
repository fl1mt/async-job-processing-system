package com.fl1mt.jobservice.domain;

import com.fl1mt.jobservice.api.CreateJobRequest;
import com.fl1mt.jobservice.api.JobResponse;
import com.fl1mt.jobservice.api.JobStatusResponse;
import com.fl1mt.jobservice.domain.outbox.JobOutboxEvent;
import com.fl1mt.jobservice.domain.outbox.OutboxEventJpaRepository;
import com.fl1mt.jobservice.domain.outbox.OutboxStatus;
import com.fl1mt.jobservice.redis.JobStatusCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobJpaRepository jobJpaRepository;
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final JobMapper jobMapper;
    private final JobStatusCacheService jobStatusCacheService;

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        Job job = jobMapper.toEntity(request);
        job.setStatus(JobStatus.CREATED);
        job.setResult(BigInteger.ZERO);
        jobJpaRepository.save(job);

        JobOutboxEvent jobOutboxEvent = new JobOutboxEvent();
        jobOutboxEvent.setEventId(UUID.randomUUID());
        jobOutboxEvent.setEventType("JOB_CREATED");
        jobOutboxEvent.setAggregateType("JOB");
        jobOutboxEvent.setAggregateId(job.getId());
        jobOutboxEvent.setStatus(OutboxStatus.NEW);

        String payload = """
                {
                "jobId": %d,
                "payload": %d
                }
                """.formatted(job.getId(), job.getPayload());
        jobOutboxEvent.setPayload(payload);
        outboxEventJpaRepository.save(jobOutboxEvent);

        return jobMapper.toResponse(job);
    }

    public List<JobResponse> getJobs() {
        return jobMapper.toListResponse(jobJpaRepository.findAll());
    }

    public JobResponse getJob(Long jobId) {
        Job job = jobJpaRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));
        return jobMapper.toResponse(job);
    }

    public JobStatusResponse getJobStatus(Long jobId) {
        Optional<String> cached = jobStatusCacheService.getStatus(jobId);
        if (cached.isPresent()) {
            log.info("Redis. Get status from cache.");
            return new JobStatusResponse(jobId, cached.get());
        }

        Job job = jobJpaRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        jobStatusCacheService.setStatus(jobId, job.getStatus().name());
        log.info("Redis. Set status in cache from JobService.");
        return new JobStatusResponse(jobId, job.getStatus().toString());
    }
}
