package com.fl1mt.jobservice.api;

import com.fl1mt.jobservice.domain.JobStatus;

import java.time.LocalDateTime;

public record JobResponse(
        Long id,
        Long payload,
        JobStatus status,
        Long result,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
