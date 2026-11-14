package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import java.util.Objects;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.utils.LogScopes;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.hud.controller.HudController;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hover.utils.GlyphStyler;

public final class FlycastingHover {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final HytaleLogger DRAW_LOG = HytaleLogger.get(LogScopes.DRAW);
    private static final float HOVER_SCALE = 1.2f;

    private FlycastingHover() {
    }

    public static void applyHexHover(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state, @Nullable Ref<EntityStore> hovered) {
        if (state.getHoveredGlyph() != null) {
            return;
        }
        var previous = state.getHoveredHex();
        if (Objects.equals(previous, hovered)) {
            return;
        }
        setHexHover(buffer, previous, false);
        state.setHoveredHex(hovered);
        if (hovered != null) {
            state.setLastHoveredHex(hovered);
            setHexHover(buffer, hovered, true);
        }
        applyHoverInfo(buffer, player, hexOf(buffer, hovered));
    }

    private static void applyHoverInfo(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            @Nullable HexComponent hovered) {
        var name = hovered != null && hovered.getHex() != null ? hovered.getHex().getDisplayName() : null;
        if (name == null || name.isBlank()) {
            HudController.hideInfo(buffer, player);
            return;
        }
        HudController.showInfo(buffer, player, Message.raw(name), null);
    }

    public static void applyGlyphHover(CommandBuffer<EntityStore> buffer, FlycastingState state,
            @Nullable Ref<EntityStore> hovered) {
        var previous = state.getHoveredGlyph();
        if (Objects.equals(previous, hovered)) {
            return;
        }
        var previousGlyph = glyphOf(buffer, previous);
        if (previousGlyph != null) {
            GlyphStyler.exitGlyphHover(buffer, previousGlyph);
        }
        state.setHoveredGlyph(hovered);
        var hoveredGlyph = glyphOf(buffer, hovered);
        DRAW_LOG.atFine().log("glyph hover: %s", hoveredGlyph != null ? hoveredGlyph.getId() : "none");
        if (hoveredGlyph != null) {
            GlyphStyler.enterGlyphHover(buffer, hoveredGlyph);
        }
    }

    private static void setHexHover(CommandBuffer<EntityStore> buffer, @Nullable Ref<EntityStore> hexRef,
            boolean hovering) {
        var hex = hexOf(buffer, hexRef);
        if (hex == null) {
            return;
        }
        try {
            hex.setHoverState(hovering);
            var firstGlyphRef = hex.getChildGlyphRef(hex.getHex().getFirstGlyphId());
            GlyphStyler.updateScale(buffer, firstGlyphRef,
                    hovering ? hex.getScale() * HOVER_SCALE : hex.getScale());
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error updating hex hover state");
        }
    }

    @Nullable
    static HexComponent hexOf(CommandBuffer<EntityStore> buffer, @Nullable Ref<EntityStore> ref) {
        return ref != null && ref.isValid() ? buffer.getComponent(ref, HexComponent.getComponentType()) : null;
    }

    @Nullable
    static GlyphComponent glyphOf(CommandBuffer<EntityStore> buffer, @Nullable Ref<EntityStore> ref) {
        return ref != null && ref.isValid() ? buffer.getComponent(ref, GlyphComponent.getComponentType()) : null;
    }
}
