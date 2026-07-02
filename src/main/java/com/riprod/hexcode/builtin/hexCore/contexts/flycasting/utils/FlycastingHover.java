package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.state.casting.utils.GlyphStyler;

public final class FlycastingHover {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float HOVER_SCALE = 1.2f;

    private FlycastingHover() {
    }

    public static void applyHexHover(CommandBuffer<EntityStore> buffer, FlycastingState state,
            HexComponent hovered) {
        if (state.getHoveredGlyph() != null) {
            return;
        }
        HexComponent previous = state.getHoveredHex();
        if (previous == hovered) {
            return;
        }
        if (previous != null) {
            setHexHover(buffer, previous, false);
        }
        state.setHoveredHex(hovered);
        if (hovered != null) {
            state.setLastHoveredHex(hovered);
            setHexHover(buffer, hovered, true);
        }
    }

    public static void applyGlyphHover(CommandBuffer<EntityStore> buffer, FlycastingState state,
            GlyphComponent hovered) {
        GlyphComponent previous = state.getHoveredGlyph();
        if (previous == hovered) {
            return;
        }
        if (previous != null) {
            GlyphStyler.exitGlyphHover(buffer, previous);
        }
        state.setHoveredGlyph(hovered);
        if (hovered != null) {
            GlyphStyler.enterGlyphHover(buffer, hovered);
        }
    }

    private static void setHexHover(CommandBuffer<EntityStore> buffer, HexComponent hex, boolean hovering) {
        try {
            hex.setHoverState(hovering);
            Ref<EntityStore> firstGlyphRef = hex.getChildGlyphRef(hex.getHex().getFirstGlyphId());
            GlyphStyler.updateScale(buffer, firstGlyphRef,
                    hovering ? hex.getScale() * HOVER_SCALE : hex.getScale());
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error updating hex hover state");
        }
    }
}
