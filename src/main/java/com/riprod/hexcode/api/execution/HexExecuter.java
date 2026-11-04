package com.riprod.hexcode.api.execution;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.BranchEndEvent;
import com.riprod.hexcode.api.event.CastEndEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.execution.CoreHexExecuter;
import com.riprod.hexcode.core.common.execution.cast.CastAssembler;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.component.CastHexComponent;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastHolder;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;
import com.riprod.hexcode.core.common.execution.resource.HexExecutionQueue;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.utils.LogScopes;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

public class HexExecuter {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CAST);

    private HexExecuter() {
    }

    public static void cast(CastHolder holder, CommandBuffer<EntityStore> buffer) {
        HexComponentRegistry.freeze();
        var root = holder.getRoot();
        if (root == null || !root.isValid())
            return;

        var assembler = new CastAssembler(holder, buffer);
        var hexComponent = assembler.get(CastHexComponent.TYPE);
        if (assembler.isAborted() || hexComponent == null)
            return;
        var hex = hexComponent.getHex().clone();
        hexComponent.setHex(hex);
        HexUtils.validate(hex);

        var components = new CastComponent[HexComponentRegistry.size()];
        for (var i = 0; i < components.length; i++) {
            components[i] = assembler.get(HexComponentRegistry.getType(i));
            if (assembler.isAborted())
                return;
        }

        var cast = new HexCast(root, components);
        var context = new HexContext(cast, components.clone(), buffer);
        var casts = buffer.getResource(HexCastStore.getResourceType());
        casts.register(cast);

        var event = new CastEvent(context);
        HytaleServer.get().getEventBus().dispatchFor(CastEvent.class).dispatch(event);
        if (event.isCancelled()) {
            endCast(cast, CastEndEvent.Reason.CANCELLED, buffer);
            return;
        }

        var first = context.getHex().getFirstGlyphId();
        if (first == null) {
            fail(null, context, GlyphFizzleEvent.Reason.ERROR);
            return;
        }
        CoreHexExecuter.continueExecution(List.of(first), context);
    }

    public static void continueFromSlot(Glyph glyph, String slotKey, HexContext hexContext) {
        CoreHexExecuter.continueFromSlot(glyph, slotKey, hexContext);
    }

    public static boolean branchFromSlot(Glyph glyph, String slotKey, HexContext hexContext) {
        return CoreHexExecuter.branchFromSlot(glyph, slotKey, hexContext);
    }

    public static void fail(HexContext hexContext) {
        fail(null, hexContext, GlyphFizzleEvent.Reason.VOLATILITY_DEPLETED);
    }

    public static void fail(Glyph glyph, HexContext hexContext) {
        fail(glyph, hexContext, GlyphFizzleEvent.Reason.VOLATILITY_DEPLETED);
    }

    public static void fail(Glyph glyph, HexContext hexContext, GlyphFizzleEvent.Reason reason) {
        fail(glyph, hexContext, reason, null, null);
    }

    public static void fail(Glyph glyph, HexContext hexContext, GlyphFizzleEvent.Reason reason,
            String detail) {
        fail(glyph, hexContext, reason, detail, null);
    }

    public static void fail(Glyph glyph, HexContext hexContext, GlyphFizzleEvent.Reason reason,
            Throwable cause) {
        fail(glyph, hexContext, reason, null, cause);
    }

    public static void fail(Glyph glyph, HexContext hexContext, GlyphFizzleEvent.Reason reason,
            String detail, Throwable cause) {
        HytaleServer.get().getEventBus().dispatchFor(GlyphFizzleEvent.class)
                .dispatch(new GlyphFizzleEvent(glyph, reason, hexContext, detail, cause));
        if (hexContext != null) {
            endBranch(hexContext);
        }
    }

    public static void endBranch(@Nonnull HexContext hexContext) {
        var buffer = hexContext.getAccessor();
        if (buffer == null) {
            LOGGER.atSevere().log("endBranch with null accessor; closing branch %d of cast %s without hooks",
                    hexContext.getBranchId(), hexContext.getExecutionId());
            hexContext.release();
            hexContext.cast().closeBranch(hexContext.getBranchId());
            return;
        }
        if (!closeBranch(hexContext, buffer)) {
            return;
        }
        var cast = hexContext.cast();
        if (cast.getActiveBranchCount() == 0) {
            endCast(cast, buffer);
        }
    }

    public static void endCast(@Nonnull HexCast cast, @Nonnull CommandBuffer<EntityStore> buffer) {
        var depleted = cast.get(VolatilityComponent.TYPE).getCurrent() <= 0f;
        endCast(cast, depleted ? CastEndEvent.Reason.DEPLETED : CastEndEvent.Reason.COMPLETED, buffer);
    }

    public static void endCast(@Nonnull HexCast cast, @Nonnull CastEndEvent.Reason reason,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        if (!cast.markEnded()) {
            return;
        }
        try {
            for (var branch = cast.firstLiveBranch(); branch != null; branch = cast.firstLiveBranch()) {
                closeBranch(branch, buffer);
            }
            HexComponentRegistry.dispatchCastEnd(cast, buffer);
            var dispatcher = HytaleServer.get().getEventBus().dispatchFor(CastEndEvent.class);
            if (dispatcher.hasListener()) {
                dispatcher.dispatch(new CastEndEvent(cast, reason, buffer));
            }
        } finally {
            cast.get(VolatilityComponent.TYPE).setCurrent(0f);
            buffer.getResource(HexExecutionQueue.getResourceType()).purge(cast);
            buffer.getResource(HexCastStore.getResourceType()).remove(cast.getExecutionId());
        }
    }

    public static void release(@Nonnull HexContext hexContext, @Nonnull UUID constructId,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        if (constructId.equals(hexContext.getHolder())) {
            release(hexContext, buffer);
        }
    }

    private static void release(@Nonnull HexContext hexContext, @Nonnull CommandBuffer<EntityStore> buffer) {
        var host = hexContext.release();
        if (host != null && hexContext.isLive()) {
            HexComponentRegistry.dispatchRelease(host, hexContext, buffer);
        }
    }

    private static boolean closeBranch(@Nonnull HexContext branch, @Nonnull CommandBuffer<EntityStore> buffer) {
        release(branch, buffer);
        if (!branch.cast().closeBranch(branch.getBranchId())) {
            return false;
        }
        HexComponentRegistry.dispatchBranchEnd(branch, buffer);
        var dispatcher = HytaleServer.get().getEventBus().dispatchFor(BranchEndEvent.class);
        if (dispatcher.hasListener()) {
            dispatcher.dispatch(new BranchEndEvent(branch, buffer));
        }
        return true;
    }

    public static void continueExecution(List<String> nextGlyphs, HexContext hexContext) {
        CoreHexExecuter.continueExecution(nextGlyphs, hexContext);
    }

    public static boolean branchExecution(List<String> nextGlyphs, HexContext hexContext) {
        return CoreHexExecuter.branchExecution(nextGlyphs, hexContext);
    }
}
