package com.fl1mt.workerservice.domain;

import com.fl1mt.events.JobCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobProcessor {

    private final WorkerOutboxEventJpaRepository repository;

    @Transactional
     public BigInteger process(JobCreatedEvent event){
        BigInteger result = calculate(event.payload());

        if (result.compareTo(BigInteger.ZERO) <= 0){
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

    private BigInteger calculate(long n){

        long parts = n / 4;

        CompletableFuture<BigInteger> p1 =
                CompletableFuture.supplyAsync(() -> sumFormula(1, parts));

        CompletableFuture<BigInteger> p2 =
                CompletableFuture.supplyAsync(() -> sumFormula(parts + 1, parts * 2));

        CompletableFuture<BigInteger> p3 =
                CompletableFuture.supplyAsync(() -> sumFormula(parts * 2 + 1, parts * 3));

        CompletableFuture<BigInteger> p4 =
                CompletableFuture.supplyAsync(() -> sumFormula(parts * 3 + 1, n));

        CompletableFuture.allOf(p1, p2, p3, p4).join();

        return p1.join()
                .add(p2.join())
                .add(p3.join())
                .add(p4.join());
    }

    private BigInteger sumFormula(long start, long end){

        BigInteger a = BigInteger.valueOf(end)
                .multiply(BigInteger.valueOf(end + 1))
                .divide(BigInteger.valueOf(2));

        BigInteger b = BigInteger.valueOf(start - 1)
                .multiply(BigInteger.valueOf(start))
                .divide(BigInteger.valueOf(2));

        return a.subtract(b);
    }
}
