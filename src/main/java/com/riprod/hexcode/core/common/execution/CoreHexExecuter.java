package com.riprod.hexcode.core.common.execution;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.BranchEndEvent;
import com.riprod.hexcode.api.event.GlyphEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.api.execution.IllegalOperationException;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.resource.HexExecutionQueue;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class CoreHexExecuter {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private CoreHexExecuter() {
    }

    /**
     * Continues the execution of the branch from a specific slot - adding the
     * glyphs to the queue for tick-deferral and handles proper branch
     * copying/tracking.
     * Ends branch if list is empty.
     */
    public static boolean continueFromSlot(Glyph glyph, String slotKey, HexContext hexContext) {
        return continueExecution(glyph.getLinks(slotKey), hexContext);
    }

    /**
     * Branches off of the primary execution flow to execute any glyphs connected to
     * the slot in parallel.
     * Adds glyphs to the execution queue and returns true if any glyphs were added,
     * false otherwise.
     */
    public static boolean branchFromSlot(Glyph glyph, String slotKey, HexContext hexContext) {
        return branchExecution(glyph.getLinks(slotKey), hexContext);
    }

    /**
     * Continues the execution of the branch - adding the glyphs to the queue for
     * tick-deferral and handles proper branch copying/tracking.
     * Ends branch if list is empty.
     */
    public static boolean continueExecution(List<String> nextGlyphs, HexContext hexContext) {
        if (refuseHeld("continueExecution", nextGlyphs, hexContext)) {
            return false;
        }
        if (nextGlyphs.isEmpty()) {
            HexExecuter.endBranch(hexContext);
            return false;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (accessor == null) {
            LOGGER.atSevere().log("continueExecution with null accessor; dropping %d glyph(s)", nextGlyphs.size());
            HexExecuter.endBranch(hexContext);
            return false;
        }

        var queue = accessor.getResource(HexExecutionQueue.getResourceType());
        queue.enqueue(new HexExecutionQueue.PendingGlyph(nextGlyphs.get(0), hexContext));
        if (nextGlyphs.size() > 1) { // clones the ctx for each subsequent branch
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
        if (refuseHeld("branchExecution", nextGlyphs, hexContext)) {
            return false;
        }
        if (nextGlyphs.isEmpty()) {
            return false;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (accessor == null) {
            LOGGER.atSevere().log("branchExecution with null accessor; dropping %d glyph(s)", nextGlyphs.size());
            return false;
        }

        HexExecutionQueue queue = accessor.getResource(HexExecutionQueue.getResourceType());
        for (var nextNodeId : nextGlyphs) {
            queue.enqueue(new HexExecutionQueue.PendingGlyph(nextNodeId, hexContext.branch()));
        }
        return true;
    }

    private static boolean refuseHeld(String operation, List<String> nextGlyphs, HexContext hexContext) {
        if (!hexContext.isHeld()) {
            return false;
        }
        LOGGER.atSevere().log("%s refused: branch %d of cast %s is held by construct %s; dropping %d glyph(s)",
                operation, hexContext.getBranchId(), hexContext.getExecutionId(), hexContext.getHolder(),
                nextGlyphs.size());
        return true;
    }

    public static void executeQueuedGlyph(String nodeId, HexContext hexContext) {
        Glyph nextNode = hexContext.getGlyph(nodeId);

        if (nextNode == null) {
            HexExecuter.fail(null, hexContext, GlyphFizzleEvent.Reason.ERROR, "unresolved node: " + nodeId);
            return;
        }
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(nextNode.getGlyphId());
        if (asset == null) {
            HexExecuter.fail(nextNode, hexContext, GlyphFizzleEvent.Reason.NOT_IMPLEMENTED);
            return;
        }
        if (!asset.isEnabled()) {
            HexExecuter.fail(nextNode, hexContext, GlyphFizzleEvent.Reason.GLYPH_DISABLED);
            return;
        }
        GlyphEvent event = new GlyphEvent(nextNode, asset, hexContext);
        HytaleServer.get().getEventBus().dispatchFor(GlyphEvent.class).dispatch(event);
        if (event.isCancelled()) {
            return;
        }
        if (!HexComponentRegistry.dispatchGlyph(nextNode, hexContext, hexContext.getAccessor())) {
            return;
        }
        try {
            asset.execute(nextNode, hexContext);
        } catch (IllegalOperationException e) {
            HexExecuter.endBranch(hexContext, BranchEndEvent.Reason.ERROR);
        } catch (Exception e) {
            HexExecuter.fail(nextNode, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED, e);
        }
    }
}
