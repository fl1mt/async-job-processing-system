package com.fl1mt.jobservice.domain;

import com.fl1mt.jobservice.domain.outbox.JobOutboxEvent;
import com.fl1mt.jobservice.domain.outbox.OutboxEventJpaRepository;
import com.fl1mt.jobservice.domain.outbox.OutboxStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private final JobJpaRepository jobJpaRepository;
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final JobMapper jobMapper;

    public JobService(JobJpaRepository jobJpaRepository, OutboxEventJpaRepository outboxEventJpaRepository, JobMapper jobMapper) {
        this.jobJpaRepository = jobJpaRepository;
        this.outboxEventJpaRepository = outboxEventJpaRepository;
        this.jobMapper = jobMapper;
    }

    @Transactional
    public JobResponse createJob(CreateJobRequest request){
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
    public JobResponse getJob(Long jobId){
        Job job = jobJpaRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found!"));
        return jobMapper.toResponse(job);
    }
}
