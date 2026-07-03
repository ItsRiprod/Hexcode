package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.GlyphCommitEvent;
import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.drawing.DrawCaptureService;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphResolver;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.drawing.utils.DraftFeedback;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.HexSpawner;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.InAirHexFactory;

public final class FlycastingCommit {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private FlycastingCommit() {
    }

    public static void commitShape(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state, ShapeStructure structure) {
        GlyphAsset matched = GlyphResolver.resolve(structure);
        if (matched == null) {
            DraftFeedback.playFailFeedback(buffer, player);
            return;
        }

        Glyph glyph = new Glyph(matched, structure.getVolatility(), structure.getEfficiency());
        GlyphCommitEvent commit = new GlyphCommitEvent(player, glyph, matched, FlycastingState.CONTEXT_ID);
        buffer.invoke(player, commit);
        if (commit.isCancelled()) {
            return;
        }

        emitGlyphDrawn(player, glyph, structure, matched);
        spawnInAirHex(buffer, player, state, matched, structure);
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
            GlyphAsset matched = GlyphResolver.resolve(structure);
            if (matched == null) {
                DraftFeedback.playFailFeedback(buffer, player);
                return null;
            }
            emitGlyphDrawn(player, new Glyph(matched, structure.getVolatility(), structure.getEfficiency()),
                    structure, matched);
            return InAirHexFactory.wrap(matched, structure.getVolatility(), structure.getEfficiency());
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Failed to finalize draft on flycasting exit");
            return null;
        }
    }

    private static void spawnInAirHex(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state, GlyphAsset matched, ShapeStructure structure) {
        Ref<EntityStore> castingRootRef = state.getCastingRootRef();
        if (castingRootRef == null || !castingRootRef.isValid()) {
            return;
        }

        Hex hex = InAirHexFactory.wrap(matched, structure.getVolatility(), structure.getEfficiency());
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

    private static void emitGlyphDrawn(Ref<EntityStore> player, Glyph glyph, ShapeStructure structure,
            GlyphAsset matched) {
        HytaleServer.get().getEventBus().dispatchFor(GlyphDrawnEvent.class)
                .dispatch(new GlyphDrawnEvent(player, glyph, structure.getShapes(), matched));
    }
}
