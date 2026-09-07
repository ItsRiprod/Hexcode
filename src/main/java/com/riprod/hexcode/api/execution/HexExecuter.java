package com.riprod.hexcode.api.execution;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.CoreHexExecuter;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HexExecuter {
    private HexExecuter() {
    }

    private static final List<CastTransform> CAST_TRANSFORMS = new CopyOnWriteArrayList<>();

    public static void registerCastTransform(CastTransform transform) {
        CAST_TRANSFORMS.add(transform);
    }

    /**
     * Casts a hex with the provided context and buffer. Invokes the hexCastEvent
     * 
     * 
     * 
     * @param context
     * @param buffer
     */
    public static void cast(HexContext context, ComponentAccessor<EntityStore> buffer) {
        if (buffer instanceof CommandBuffer<EntityStore> buff) {
            context.updateRuntimeAccessors(buff);
        }

        if (context.getStyle() == null)
            context.setStyle(HexStyleAsset.empty());

        HexCast cast = context.cast();
        if (cast != null && cast.getHex() != null) {
            cast.setHex(cast.getHex().clone());
            for (CastTransform transform : CAST_TRANSFORMS) {
                transform.apply(context, buffer);
            }
        }
        HexCastStore casts = buffer.getResource(HexCastStore.getResourceType());
        if (cast != null)
            casts.register(cast);

        HexCastEvent.Pre pre = new HexCastEvent.Pre(context);
        buffer.invoke(pre);
        if (pre.isCancelled()) {
            if (cast != null)
                casts.remove(cast.getExecutionId());
            return;
        }
        buffer.invoke(new HexCastEvent(context));
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
