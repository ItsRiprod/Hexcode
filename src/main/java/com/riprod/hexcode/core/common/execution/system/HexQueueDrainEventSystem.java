package com.riprod.hexcode.core.common.execution.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexQueueDrainEvent;
import com.riprod.hexcode.config.HexcodeConfig;
import com.riprod.hexcode.core.common.execution.CoreHexExecuter;
import com.riprod.hexcode.core.common.execution.cast.component.GlyphBudgetComponent;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.resource.HexExecutionQueue;
import com.riprod.hexcode.core.common.execution.resource.HexExecutionQueue.PendingGlyph;
import com.riprod.hexcode.utils.LogScopes;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class HexQueueDrainEventSystem extends WorldEventSystem<EntityStore, HexQueueDrainEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CAST);

    public HexQueueDrainEventSystem() {
        super(HexQueueDrainEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexQueueDrainEvent event) {
        HexExecutionQueue queue = store.getResource(HexExecutionQueue.getResourceType());
        HexcodeConfig config = HexcodeConfig.get();
        int maxPerTick = config.getMaxGlyphsPerTick();
        int maxPerCast = config.getMaxGlyphsPerCast();
        long deadline = System.nanoTime() + config.getMaxGlyphNanosPerTick();
        long tick = queue.getTick();
        int executed = 0;
        int index = 0;
        boolean truncated = false;
        boolean timedOut = false;

        ObjectArrayList<PendingGlyph> work = queue.beginDrain();
        try {
            for (; index < work.size() && executed < maxPerTick; index++) {
                if (executed > 0 && (executed & 15) == 0 && System.nanoTime() >= deadline) {
                    timedOut = true;
                    break;
                }
                PendingGlyph item = work.get(index);
                HexContext ctx = item.ctx();
                HexCast cast = ctx.cast();
                GlyphBudgetComponent budget = ctx.get(GlyphBudgetComponent.TYPE);
                if (!budget.trySpend(tick, maxPerCast)) {
                    if (budget.getDenied() == 1) {
                        LOGGER.atFine().log(
                                "cast %s reached its per-tick cap of %d glyph(s); deferring the remainder",
                                cast.getExecutionId(), maxPerCast);
                    }
                    queue.defer(item);
                    continue;
                }
                ctx.UpdateAccessor(buffer);
                CoreHexExecuter.executeQueuedGlyph(item.glyphId(), ctx);
                executed++;
            }
            truncated = index < work.size();
        } finally {
            queue.endDrain(index);
        }

        if (truncated) {
            LOGGER.atFine().log("world reached its per-tick %s cap (%d glyph(s), %d ns budget); %d still queued",
                    timedOut ? "time" : "count", maxPerTick, config.getMaxGlyphNanosPerTick(), queue.size());
        }
    }
}
