package com.fl1mt.events;

import java.math.BigInteger;

public record JobCompletedEvent (Long jobId, BigInteger result) {
}
