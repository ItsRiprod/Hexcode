package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.drawing.DrawAnchorUtils;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.positioning.GlyphSelector;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.HexSpawner;
import com.riprod.hexcode.utils.GlyphMath;

public final class FlycastingDragHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private FlycastingDragHandler() {
    }

    public static void beginDrag(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state, DrawCaptureComponent capture, HexComponent hex) {
        Ref<EntityStore> headAnchorRef = DrawAnchorUtils.ensureAnchor(buffer, player, capture);

        Ref<EntityStore> glyphRef = hex.getSelfRef();
        if (glyphRef != null && glyphRef.isValid()) {
            buffer.tryRemoveComponent(glyphRef, MountedComponent.getComponentType());
            buffer.addComponent(glyphRef, MountedComponent.getComponentType(),
                    new MountedComponent(headAnchorRef, new Rotation3f(0, 0, -hex.getDistance()),
                            MountController.Minecart));
        }
        state.setDraggingHex(hex);
    }

    public static void tickDrag(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state, HeadRotation headRotation) {
        GlyphSelector.DragGlyph(buffer, player, state.getDraggingHex());

        HexComponent targetHex = GlyphSelector.findHoveredHex(buffer, headRotation.getRotation(),
                state.getActiveHexes());
        FlycastingHover.applyHexHover(buffer, state, targetHex);

        GlyphComponent targetGlyph = null;
        if (targetHex != null && targetHex != state.getDraggingHex()) {
            targetGlyph = GlyphSelector.findHoveredGlyph(buffer, headRotation.getRotation(), targetHex);
            if (targetGlyph != null) {
                GlyphComponent outputChild = GlyphSelector.findOutputChild(buffer, targetHex, targetGlyph);
                if (outputChild != null) {
                    targetGlyph = outputChild;
                }
            }
        }
        FlycastingHover.applyGlyphHover(buffer, state, targetGlyph);
    }

    public static void endDrag(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state) {
        HexComponent draggedHex = state.getDraggingHex();
        if (draggedHex == null) {
            return;
        }

        GlyphComponent hoveredGlyph = state.getHoveredGlyph();
        if (hoveredGlyph != null) {
            try {
                float eyeHeight = FlycastingScene.resolveEyeHeight(buffer, player);
                HexSpawner.MergeGlyphs(buffer, hoveredGlyph, draggedHex, eyeHeight);
                state.getActiveHexes().remove(draggedHex.getSelfRef());
                state.setDraggingHex(null);
                FlycastingHover.applyGlyphHover(buffer, state, null);
                return;
            } catch (Exception e) {
                LOGGER.atWarning().withCause(e).log("Error merging glyphs, dropping instead");
            }
        }

        state.setDraggingHex(null);
        FlycastingHover.applyHexHover(buffer, state, null);
        FlycastingHover.applyGlyphHover(buffer, state, null);

        Rotation3f dhr = draggedHex.getRotation();
        Vector3d dropPos = GlyphMath.sphericalToCartesian(dhr);
        draggedHex.setOffset(new Vector3f((float) dropPos.x, (float) dropPos.y, (float) dropPos.z));

        Vector3f doff = draggedHex.getOffset();
        buffer.putComponent(draggedHex.getSelfRef(), MountedComponent.getComponentType(),
                new MountedComponent(draggedHex.getRootRef(),
                        new Rotation3f(doff.x, doff.y, doff.z),
                        MountController.Minecart));
    }

}
