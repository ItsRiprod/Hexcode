package com.riprod.hexcode.core.common.execution.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class VolatilityTrackerTest {

    @Test
    void copyPreservesRuntimeIdentity() {
        VolatilityTracker original = new VolatilityTracker(10f, 1.0f, 2.0f);
        UUID executionId = UUID.randomUUID();
        original.setExecutionId(executionId);
        original.setSlotKey("book:1");

        VolatilityTracker copy = original.copy();

        assertEquals(10f, copy.getStartingBudget());
        assertEquals(2.0f, copy.getMagicPowerMultiplier());
        assertEquals(executionId, copy.getExecutionId());
        assertEquals("book:1", copy.getSlotKey());
    }
}
