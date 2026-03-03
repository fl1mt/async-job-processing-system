package com.fl1mt.jobservice.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateJobRequest(
        @NotNull
                @Min(100000)
        Long payload
) {
}
