package com.riprod.hexcode.core.common.execution.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class VolatilityTrackerTest {

    @Test
    void copyPreservesRuntimeIdentityAndUsageWithoutSharingUsageMap() {
        VolatilityTracker original = new VolatilityTracker(10f, 1.0f, 2.0f);
        UUID executionId = UUID.randomUUID();
        original.setExecutionId(executionId);
        original.setSlotKey("book:1");
        original.incrementGlyphUsage("Beam");
        original.incrementGlyphUsage("Beam");

        VolatilityTracker copy = original.copy();
        copy.incrementGlyphUsage("Beam");

        assertEquals(10f, copy.getStartingBudget());
        assertEquals(2.0f, copy.getMagicPowerMultiplier());
        assertEquals(executionId, copy.getExecutionId());
        assertEquals("book:1", copy.getSlotKey());
        assertEquals(2, original.getGlyphUsage("Beam"));
        assertEquals(3, copy.getGlyphUsage("Beam"));
    }
}
