package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.drawing.DrawCaptureService;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.utils.DraftFeedback;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphResolver;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public final class FlycastingCommit {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private FlycastingCommit() {
    }

    @Nullable
    public static Hex finalizeDraft(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            @Nullable DrawCaptureComponent capture) {
        if (capture == null || capture.getPendingShapes().isEmpty()) {
            return null;
        }
        try {
            ShapeStructure structure = DrawCaptureService.computeStructure(capture.getPendingShapes());
            capture.getPendingShapes().clear();
            capture.setFinalizePending(false);
            var resolution = GlyphResolver.resolve(buffer, player, structure, FlycastingState.CONTEXT_ID);
            if (resolution.status() == GlyphResolver.Status.NO_MATCH) {
                DraftFeedback.playFailFeedback(buffer, player);
                return null;
            }
            if (!resolution.isResolved()) {
                return null;
            }
            emitGlyphDrawn(player, resolution.glyph(), structure.getShapes(), resolution.asset());
            return InAirHexFactory.wrap(resolution.glyph());
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Failed to finalize draft on flycasting exit");
            return null;
        }
    }

    public static void spawnInAirHex(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state, Glyph glyph) {
        Ref<EntityStore> castingRootRef = state.getCastingRootRef();
        if (castingRootRef == null || !castingRootRef.isValid()) {
            return;
        }

        Hex hex = InAirHexFactory.wrap(glyph);
        if (hex == null) {
            DraftFeedback.playFailFeedback(buffer, player);
            return;
        }

        Ref<EntityStore> hexRef = HexSpawner.spawnSingleHex(buffer, player, castingRootRef, hex);
        if (hexRef == null) {
            DraftFeedback.playFailFeedback(buffer, player);
            return;
        }
        state.getActiveHexes().add(hexRef);
    }

    public static void emitGlyphDrawn(Ref<EntityStore> player, Glyph glyph,
            List<DrawnShapeComponent> shapes, GlyphAsset matched) {
        HytaleServer.get().getEventBus().dispatchFor(GlyphDrawnEvent.class)
                .dispatch(new GlyphDrawnEvent(player, glyph, shapes, matched));
    }
}
