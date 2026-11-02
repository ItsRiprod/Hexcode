package com.riprod.hexcode.api.execution;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.execution.CoreHexExecuter;
import com.riprod.hexcode.core.common.execution.cast.CastAssembler;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.component.CastHexComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastHolder;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;

import java.util.List;

public class HexExecuter {
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
            casts.remove(cast.getExecutionId());
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
            hexContext.endBranch();
        }
    }

    public static void continueExecution(List<String> nextGlyphs, HexContext hexContext) {
        CoreHexExecuter.continueExecution(nextGlyphs, hexContext);
    }

    public static boolean branchExecution(List<String> nextGlyphs, HexContext hexContext) {
        return CoreHexExecuter.branchExecution(nextGlyphs, hexContext);
    }
}
