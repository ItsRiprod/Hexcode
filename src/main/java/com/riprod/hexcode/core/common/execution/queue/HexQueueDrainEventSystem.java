package com.riprod.hexcode.core.common.execution.queue;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexQueueDrainEvent;
import com.riprod.hexcode.core.common.execution.CoreHexExecuter;
import com.riprod.hexcode.core.common.execution.cast.GlyphBudgetComponent;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue.PendingGlyph;
import com.riprod.hexcode.utils.LogScopes;

public class HexQueueDrainEventSystem extends WorldEventSystem<EntityStore, HexQueueDrainEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CAST);

    public static final int MAX_GLYPHS_PER_TICK = 512;
    public static final int MAX_GLYPHS_PER_CAST = 128;

    public HexQueueDrainEventSystem() {
        super(HexQueueDrainEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexQueueDrainEvent event) {
        HexExecutionQueue queue = store.getResource(HexExecutionQueue.getResourceType());
        long tick = queue.getTick();
        int executed = 0;
        boolean truncated = false;

        try {
            for (int i = 0; i < event.getCount(); i++) {
                if (executed >= MAX_GLYPHS_PER_TICK) {
                    truncated = true;
                    break;
                }
                PendingGlyph item = queue.poll();
                if (item == null) {
                    break;
                }
                HexContext ctx = item.ctx();
                HexCast cast = ctx.cast();
                if (cast != null) {
                    GlyphBudgetComponent budget = cast.getOrCreate(GlyphBudgetComponent.getComponentType());
                    if (!budget.trySpend(tick, MAX_GLYPHS_PER_CAST)) {
                        if (budget.getDenied() == 1) {
                            LOGGER.atFine().log(
                                    "cast %s reached its per-tick cap of %d glyph(s); deferring the remainder",
                                    cast.getExecutionId(), MAX_GLYPHS_PER_CAST);
                        }
                        queue.defer(item);
                        continue;
                    }
                }
                ctx.UpdateAccessor(buffer);
                CoreHexExecuter.executeQueuedGlyph(item.glyphId(), ctx);
                executed++;
            }
        } finally {
            queue.restoreDeferred();
        }

        if (truncated) {
            LOGGER.atFine().log("world reached its per-tick cap of %d glyph(s); %d still queued",
                    MAX_GLYPHS_PER_TICK, queue.size());
        }
    }
}
