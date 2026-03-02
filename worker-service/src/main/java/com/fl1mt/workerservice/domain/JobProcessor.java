package com.fl1mt.workerservice.domain;

import com.fl1mt.events.JobCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobProcessor {

    private final WorkerOutboxEventJpaRepository repository;

    @Transactional
    public long process(JobCreatedEvent event){
        long result = calculate(event.payload());

        if (result <= 0){
            throw new RuntimeException("Result must be more than 0!");
        }

        WorkerOutboxEvent workerOutboxEvent = new WorkerOutboxEvent();
        workerOutboxEvent.setEventId(UUID.randomUUID());
        workerOutboxEvent.setAggregateId(event.jobId());
        workerOutboxEvent.setEventType("JOB_COMPLETED");
        workerOutboxEvent.setAggregateType("JOB");
        workerOutboxEvent.setStatus(OutboxStatus.NEW);
        workerOutboxEvent.setResult(result);
        repository.save(workerOutboxEvent);
        return result;
    }

    private long calculate(long num){
        long n = num;
        long parts = n / 4;

        CompletableFuture<Long> p1 = CompletableFuture.supplyAsync(() -> sum(1, parts));
        CompletableFuture<Long> p2 = CompletableFuture.supplyAsync(() -> sum(parts + 1, parts * 2));
        CompletableFuture<Long> p3 = CompletableFuture.supplyAsync(() -> sum(parts * 2 + 1, parts * 3));
        CompletableFuture<Long> p4 = CompletableFuture.supplyAsync(() -> sum(parts * 3 + 1, n));

        CompletableFuture.allOf(p1, p2, p3, p4).join();

        return p1.join() + p2.join() + p3.join() + p4.join();
    }

    private long sum(long start, long end) {

        long result = 0;

        for (long i = start; i <= end; i++) {
            result += i;
        }

        return result;
    }
}
