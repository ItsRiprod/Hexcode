package com.riprod.hexcode.core.common.execution;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphExecuteEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
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

    /**
     * Handler for the post-gate execution after the ECS event is emitted and
     * mutated.
     * 
     * Should not be called directly, but rather through the HexExecuter.
     */
    public static void runPostGate(HexContext context, CommandBuffer<EntityStore> buffer) {
        context.updateRuntimeAccessors(buffer);

        if (context.getHexRoot() == null) {
            HytaleServer.get().getEventBus().dispatchFor(GlyphFizzleEvent.class)
                    .dispatch(new GlyphFizzleEvent(null, GlyphFizzleEvent.Reason.ERROR, context));
            return;
        }

        if (context.isConsumeMana() && !context.getHexRoot().tryConsumeMana(context.getManaCost(), buffer)) {
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

    /**
     * Continues the execution of the branch from a specific slot - adding the
     * glyphs to the queue for tick-deferral and handles proper branch
     * copying/tracking.
     * Ends branch if list is empty.
     */
    public static boolean continueFromSlot(Glyph glyph, String slotKey, HexContext hexContext) {
        Slot slot = glyph.getSlot(slotKey);
        List<String> links = slot != null ? Arrays.asList(slot.getLinks()) : List.of();
        return continueExecution(links, hexContext);
    }

    /**
     * Branches off of the primary execution flow to execute any glyphs connected to
     * the slot in parallel.
     * Adds glyphs to the execution queue and returns true if any glyphs were added,
     * false otherwise.
     */
    public static boolean branchFromSlot(Glyph glyph, String slotKey, HexContext hexContext) {
        Slot slot = glyph.getSlot(slotKey);
        if (slot == null)
            return false;
        return branchExecution(Arrays.asList(slot.getLinks()), hexContext);
    }

    /**
     * Continues the execution of the branch - adding the glyphs to the queue for
     * tick-deferral and handles proper branch copying/tracking.
     * Ends branch if list is empty.
     */
    public static boolean continueExecution(List<String> nextGlyphs, HexContext hexContext) {
        if (nextGlyphs.isEmpty()) {
            hexContext.endBranch();
            return false;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (accessor == null) {
            LOGGER.atSevere().log("continueExecution with null accessor; dropping %d glyph(s)", nextGlyphs.size());
            hexContext.endBranch();
            return false;
        }

        HexExecutionQueue queue = accessor.getResource(HexExecutionQueue.getResourceType());
        queue.enqueue(new HexExecutionQueue.PendingGlyph(nextGlyphs.get(0), hexContext));
        if (nextGlyphs.size() > 1) {
            branchExecution(nextGlyphs.subList(1, nextGlyphs.size()), hexContext);
        }
        return true;
    }

    /**
     * Branches off of the primary execution flow to execute the given glyphs in
     * parallel.
     * Adds glyphs to the execution queue and returns true if any glyphs were added,
     * false otherwise.
     */
    public static boolean branchExecution(List<String> nextGlyphs, HexContext hexContext) {
        if (nextGlyphs.isEmpty()) {
            return false;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (accessor == null) {
            LOGGER.atSevere().log("branchExecution with null accessor; dropping %d glyph(s)", nextGlyphs.size());
            return false;
        }

        HexExecutionQueue queue = accessor.getResource(HexExecutionQueue.getResourceType());
        for (String nextNodeId : nextGlyphs) {
            queue.enqueue(new HexExecutionQueue.PendingGlyph(nextNodeId, hexContext.branch()));
        }
        return true;
    }

    public static void executeQueuedGlyph(String nodeId, HexContext hexContext) {
        Glyph nextNode = hexContext.getGlyph(nodeId);

        if (nextNode == null) {
            HexExecuter.fail(null, hexContext, GlyphFizzleEvent.Reason.ERROR, "unresolved node: " + nodeId);
            return;
        }
        HytaleServer.get().getEventBus().dispatchFor(GlyphExecuteEvent.class)
                .dispatch(new GlyphExecuteEvent(nodeId, nextNode, hexContext));
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(nextNode.getGlyphId());
        if (asset != null && !asset.isEnabled()) {
            HexExecuter.fail(nextNode, hexContext, GlyphFizzleEvent.Reason.GLYPH_DISABLED);
            return;
        }
        GlyphHandler nextHandler = asset != null ? GlyphRegistry.get(asset.getHandler()) : null;
        if (nextHandler == null) {
            HexExecuter.fail(nextNode, hexContext);
            return;
        }
        try {
            nextHandler.execute0(nextNode, hexContext);
        } catch (Exception e) {
            HexExecuter.fail(nextNode, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED, e);
        }
    }
}
