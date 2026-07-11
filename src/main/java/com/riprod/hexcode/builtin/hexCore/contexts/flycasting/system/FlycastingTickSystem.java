package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.FlycastingDragHandler;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.FlycastingHover;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.positioning.GlyphPositioner;
import com.riprod.hexcode.core.common.positioning.GlyphSelector;

public class FlycastingTickSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return FlycastingState.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            FlycastingState state = chunk.getComponent(index, FlycastingState.getComponentType());
            if (state == null) {
                return;
            }
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            DrawCaptureComponent capture = chunk.getComponent(index, DrawCaptureComponent.getComponentType());

            HeadRotation headRotation = chunk.getComponent(index, HeadRotation.getComponentType());
            Ref<EntityStore> castingRootRef = state.getCastingRootRef();
            if (headRotation == null || castingRootRef == null || !castingRootRef.isValid()) {
                return;
            }

            GlyphPositioner.PositionGlyphs(buffer, player, castingRootRef);

            if (capture != null) {
                if (capture.getDraggingHex() != null && state.getDraggingHex() == null) {
                    FlycastingDragHandler.beginDrag(buffer, player, state, capture, capture.getDraggingHex());
                }
                if (capture.consumeDragReleaseRequested()) {
                    FlycastingDragHandler.endDrag(buffer, player, state);
                    capture.setDraggingHex(null);
                }
            }

            if (state.getDraggingHex() != null) {
                FlycastingDragHandler.tickDrag(buffer, player, state, headRotation);
            } else {
                HexComponent hovered = GlyphSelector.findHoveredHex(buffer,
                        headRotation.getRotation(), state.getActiveHexes());
                FlycastingHover.applyHexHover(buffer, state, hovered);
                if (capture != null) {
                    capture.setHoveredHex(state.getHoveredHex());
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] flycasting tick failed");
        }
    }
}
