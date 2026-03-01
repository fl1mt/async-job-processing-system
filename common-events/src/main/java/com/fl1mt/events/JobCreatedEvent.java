package com.fl1mt.events;

public record JobCreatedEvent(
        Long jobId,
        String payload
) {
}
