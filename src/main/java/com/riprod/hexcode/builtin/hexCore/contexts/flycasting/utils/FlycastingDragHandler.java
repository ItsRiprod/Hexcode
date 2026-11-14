package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.joml.Vector3f;

import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.drawing.DrawAnchorUtils;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.positioning.GlyphSelector;
import com.riprod.hexcode.core.common.positioning.utils.HexEntities;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.utils.GlyphMath;

public final class FlycastingDragHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private FlycastingDragHandler() {
    }

    /** Mounts the hex to the head anchor, which carries it in front of the look. */
    public static void beginDrag(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state, DrawCaptureComponent capture, Ref<EntityStore> hexRef) {
        var hex = FlycastingHover.hexOf(buffer, hexRef);
        if (hex == null) {
            return;
        }
        var headAnchorRef = DrawAnchorUtils.ensureAnchor(buffer, player, capture);
        buffer.putComponent(hexRef, MountedComponent.getComponentType(),
                new MountedComponent(headAnchorRef, new Vector3f(0, 0, -hex.getDistance()), MountController.Minecart));
        hex.setDragState(true);
        state.setDraggingHex(hexRef);
    }


    public static void tickDrag(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state, HeadRotation headRotation) {
        var head = headRotation.getRotation();
        var dragged = FlycastingHover.hexOf(buffer, state.getDraggingHex());
        if (dragged != null && (dragged.getRotation().x != head.x || dragged.getRotation().y != head.y)) {
            dragged.getRotation().x = head.x;
            dragged.getRotation().y = head.y;
            setFacing(buffer, state.getDraggingHex(), head.x, head.y);
            turnGlyphs(buffer, dragged, head.x, head.y);
        }

        var targetRef = GlyphSelector.findHoveredHex(buffer, head, state.getActiveHexes(), state.getDraggingHex());
        FlycastingHover.applyHexHover(buffer, player, state, targetRef);

        Ref<EntityStore> targetGlyph = null;
        var target = FlycastingHover.hexOf(buffer, targetRef);
        if (target != null) {
            targetGlyph = GlyphSelector.findHoveredGlyph(head, target);
            var glyph = FlycastingHover.glyphOf(buffer, targetGlyph);
            if (glyph != null) {
                var outputChild = GlyphSelector.findOutputChild(target, glyph.getId());
                if (outputChild != null) {
                    targetGlyph = outputChild;
                }
            }
        }
        FlycastingHover.applyGlyphHover(buffer, state, targetGlyph);
    }

    public static void endDrag(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            FlycastingState state) {
        var draggedRef = state.getDraggingHex();
        var draggedHex = FlycastingHover.hexOf(buffer, draggedRef);
        if (draggedHex == null) {
            state.setDraggingHex(null);
            return;
        }
        draggedHex.setDragState(false);

        var hoveredGlyph = FlycastingHover.glyphOf(buffer, state.getHoveredGlyph());
        if (hoveredGlyph != null) {
            try {
                var eyeHeight = FlycastingScene.resolveEyeHeight(buffer, player);
                HexSpawner.MergeGlyphs(buffer, hoveredGlyph, draggedHex, eyeHeight);
                state.getActiveHexes().remove(draggedRef);
                state.setDraggingHex(null);
                FlycastingHover.applyGlyphHover(buffer, state, null);
                return;
            } catch (Exception e) {
                LOGGER.atWarning().withCause(e).log("Error merging glyphs, dropping instead");
            }
        }

        state.setDraggingHex(null);
        FlycastingHover.applyHexHover(buffer, player, state, null);
        FlycastingHover.applyGlyphHover(buffer, state, null);

        var facing = draggedHex.getRotation();
        var dropPos = GlyphMath.sphericalToCartesian(facing);
        draggedHex.setOffset(new Vector3f((float) dropPos.x, (float) dropPos.y, (float) dropPos.z));
        buffer.putComponent(draggedRef, MountedComponent.getComponentType(),
                new MountedComponent(draggedHex.getRootRef(), new Vector3f(draggedHex.getOffset()),
                        MountController.Minecart));
        setFacing(buffer, draggedRef, facing.x, facing.y);
    }

    private static void turnGlyphs(CommandBuffer<EntityStore> buffer, HexComponent hex, float pitch, float yaw) {
        var state = hex.getLayoutState();
        if (state == null) {
            return;
        }
        var facing = new Rotation3f(pitch, yaw, 0f);
        var spins = HexEntities.accumulatedSpins(state.placements());
        for (var id : state.placements().keySet()) {
            var ref = hex.getChildGlyphRef(id);
            var transform = ref != null && ref.isValid()
                    ? buffer.getComponent(ref, TransformComponent.getComponentType())
                    : null;
            if (transform != null) {
                transform.getRotation().set(HexEntities.worldRotation(facing, spins.get(id)));
            }
        }
    }

    private static void setFacing(CommandBuffer<EntityStore> buffer, Ref<EntityStore> hexRef, float pitch, float yaw) {
        var transform = buffer.getComponent(hexRef, TransformComponent.getComponentType());
        if (transform != null) {
            transform.getRotation().set(pitch, yaw, 0f);
        }
    }
}
