package com.fl1mt.jobservice.domain;

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
