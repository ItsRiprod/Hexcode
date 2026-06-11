package com.riprod.hexcode.api.execution;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.CoreHexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

import java.util.Arrays;
import java.util.List;

public class HexExecuter {
    private HexExecuter() {
    }

    /**
     * Casts a hex with the provided context and buffer. Invokes the hexCastEvent
     * @param context
     * @param buffer
     */
    public static void cast(HexContext context, CommandBuffer<EntityStore> buffer) {
        context.UpdateAccessor(buffer);
        if (context.getStyle() == null) context.setStyle(HexStyleAsset.empty());
        buffer.invoke(new HexCastEvent(context));
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
    }

    public static void continueExecution(List<String> nextGlyphs, HexContext hexContext) {
        CoreHexExecuter.continueExecution(nextGlyphs, hexContext);
    }
}