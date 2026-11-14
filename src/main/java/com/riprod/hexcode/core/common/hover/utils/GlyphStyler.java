package com.riprod.hexcode.core.common.hover.utils;


import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;

import org.joml.Vector3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;

public final class GlyphStyler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final float HOVER_SCALE = 1.2f;

    public static void enterGlyphHover(CommandBuffer<EntityStore> accessor, GlyphComponent glyph) {
        try {
            glyph.setHoverState(true);
            updateScale(accessor, glyph.getSelfRef(), glyph.getScale() * HOVER_SCALE);
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error entering glyph hover state");
        }
    }

    public static void exitGlyphHover(CommandBuffer<EntityStore> accessor, GlyphComponent glyph) {
        try {
            glyph.setHoverState(false);
            updateScale(accessor, glyph.getSelfRef(), glyph.getScale());
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error exiting glyph hover state");
        }
    }

    public static void updateScale(CommandBuffer<EntityStore> accessor, Ref<EntityStore> selfRef, float newScale) {
        try {
            if (selfRef == null || !selfRef.isValid())
                return;

            var existing = accessor.getComponent(selfRef, EntityScaleComponent.getComponentType());
            if (existing != null) {
                existing.setScale(newScale);
                return;
            }

            accessor.putComponent(selfRef, EntityScaleComponent.getComponentType(), new EntityScaleComponent(newScale));
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error updating scale for glyph");
        }
    }

    public static void updateMountPosition(CommandBuffer<EntityStore> accessor, GlyphComponent glyph,
            Vector3f newPosition) {
        try {

            var newMount = new MountedComponent(glyph.getParentRef(),
                    new Vector3f(newPosition.x, newPosition.y, newPosition.z),
                    MountController.Minecart);
            accessor.putComponent(glyph.getSelfRef(), MountedComponent.getComponentType(), newMount);

        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error updating mount position for glyph");
        }
    }
}
