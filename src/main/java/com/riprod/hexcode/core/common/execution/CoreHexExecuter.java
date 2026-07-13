package com.riprod.hexcode.core.common.execution;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.VolatilityTracker;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;

import java.util.Arrays;
import java.util.List;

public class CoreHexExecuter {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private CoreHexExecuter() {
    }

    public static void runPostGate(HexContext context, CommandBuffer<EntityStore> buffer) {
        context.updateRuntimeAccessors(buffer);

        if (context.getHexRoot() == null) {
            HytaleServer.get().getEventBus().dispatchFor(GlyphFizzleEvent.class)
                    .dispatch(new GlyphFizzleEvent(null, GlyphFizzleEvent.Reason.ERROR, context));
            return;
        }

        if (!context.getHexRoot().tryConsumeMana(context.getManaCost(), buffer)) {
            HytaleServer.get().getEventBus().dispatchFor(GlyphFizzleEvent.class)
                    .dispatch(new GlyphFizzleEvent(null, GlyphFizzleEvent.Reason.INSUFFICIENT_MANA, context));
            return;
        }

        Hex hex = context.getHex();
        if (hex == null) {
            HytaleServer.get().getEventBus().dispatchFor(GlyphFizzleEvent.class)
                    .dispatch(new GlyphFizzleEvent(null, GlyphFizzleEvent.Reason.ERROR, context));
            return;
        }
        String startingGlyph = hex.getFirstGlyphId();
        if (startingGlyph == null) {
            HytaleServer.get().getEventBus().dispatchFor(GlyphFizzleEvent.class)
                    .dispatch(new GlyphFizzleEvent(null, GlyphFizzleEvent.Reason.ERROR, context));
            return;
        }

        HexVar defaultVar = context.getDefaultVariable();
        if (defaultVar == null) {
            defaultVar = context.getHexRoot().getRootVar(context);
        }
        if (defaultVar != null) {
            context.setDefaultVariable(defaultVar);
        }

        continueExecution(List.of(startingGlyph), context);
    }

    public static void continueFromSlot(Glyph glyph, String slotKey, HexContext hexContext) {
        Slot slot = glyph.getSlot(slotKey);
        if (slot == null)
            return;
        String[] links = slot.getLinks();
        if (links.length == 0)
            return;
        continueExecution(Arrays.asList(links), hexContext);
    }

    public static void continueExecution(List<String> nextGlyphs, HexContext hexContext) {
        if (nextGlyphs.isEmpty()) {
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (accessor == null) {
            LOGGER.atSevere().log("continueExecution with null accessor; dropping %d glyph(s)", nextGlyphs.size());
            return;
        }

        HexExecutionQueue queue = accessor.getResource(HexExecutionQueue.getResourceType());
        boolean multiBranch = nextGlyphs.size() > 1;

        for (String nextNodeId : nextGlyphs) {
            queue.enqueue(new HexExecutionQueue.PendingGlyph(nextNodeId,
                    multiBranch ? hexContext.branch() : hexContext));
        }
    }

    public static void drainStep(String nodeId, HexContext hexContext) {
        Glyph nextNode = hexContext.getGlyph(nodeId);

        VolatilityTracker tracker = hexContext.getVolatilityTracker();
        if (tracker != null && tracker.getRemainingBudget() <= 0) {
            HexExecuter.fail(nextNode, hexContext, GlyphFizzleEvent.Reason.VOLATILITY_DEPLETED);
            return;
        }

        if (nextNode == null) {
            HexExecuter.fail(null, hexContext);
            return;
        }
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(nextNode.getGlyphId());
        if (asset != null && !asset.isEnabled()) {
            HexExecuter.fail(nextNode, hexContext, GlyphFizzleEvent.Reason.GLYPH_DISABLED);
            return;
        }
        GlyphHandler nextHandler = GlyphRegistry.get(nextNode.getGlyphId());
        if (nextHandler == null) {
            HexExecuter.fail(nextNode, hexContext);
            return;
        }
        try {
            if (!nextHandler.consumeVolatility(nextNode, hexContext)) {
                HexExecuter.fail(nextNode, hexContext);
                return;
            }
            nextHandler.execute(nextNode, hexContext);
        } catch (Exception e) {
            HexExecuter.fail(nextNode, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED, e);
        }
    }
}
