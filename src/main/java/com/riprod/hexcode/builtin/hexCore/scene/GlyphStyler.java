package com.riprod.hexcode.builtin.hexCore.scene;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.utils.GlyphMath;

public class GlyphStyler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final float SCALE_PER_GLYPH = 0.05f; // increase scale by 5% per glyph
    public static final float SCALE_SINGLE_GLYPH = 0.45f; // if only 1 glyph, make it slightly smaller to avoid clipping
    public static final float SCALE_MULTIPLIER = 0.2f;

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

            EntityScaleComponent existing = accessor.getComponent(selfRef, EntityScaleComponent.getComponentType());
            if (existing != null) {
                existing.setScale(newScale);
                return;
            }

            accessor.putComponent(selfRef, EntityScaleComponent.getComponentType(), new EntityScaleComponent(newScale));
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error updating scale for glyph");
        }
    }

    public static void updateTransformPosition(ComponentAccessor<EntityStore> accessor, GlyphComponent glyph,
            Vector3d newPosition) {
        try {

            Ref<EntityStore> selfRef = glyph.getSelfRef();

            TransformComponent transform = accessor.getComponent(selfRef, TransformComponent.getComponentType());
            transform.setPosition(newPosition);

        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error updating position for glyph");
        }
    }

    public static void updateMountPosition(CommandBuffer<EntityStore> accessor, GlyphComponent glyph,
            Vector3f newPosition) {
        try {

            MountedComponent newMount = new MountedComponent(glyph.getParentRef(),
                    new Rotation3f(newPosition.x, newPosition.y, newPosition.z),
                    MountController.Minecart);
            accessor.putComponent(glyph.getSelfRef(), MountedComponent.getComponentType(), newMount);

        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error updating mount position for glyph");
        }
    }

    public static void enterIdleAnim(ComponentAccessor<EntityStore> accessor, HexComponent glyph) {
        try {

        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Error entering Idle animation state for glyph");
        }
    }

    public static void UpdateHexTree(CommandBuffer<EntityStore> accessor, HexComponent hexComponent,
            GlyphComponent parentGlyph) {

        int numGlyphs = (int) hexComponent.getGlyphs().stream()
                .filter(glyph -> glyph != null)
                .count();

        float scaleMultiplier = 1 + (numGlyphs * SCALE_PER_GLYPH);

        parentGlyph.setScale(scaleMultiplier);
        hexComponent.setScale(scaleMultiplier);
        UpdateGlyphTree(accessor, hexComponent, parentGlyph, new HashSet<>());
    }

    private static void UpdateGlyphTree(CommandBuffer<EntityStore> accessor, HexComponent hexComponent,
            GlyphComponent parentGlyph, Set<String> styledGlyphs) {

        List<String> flowGlyphIds = parentGlyph.getFlowLinks();

        List<Ref<EntityStore>> children = hexComponent.getChildGlyphRefs(flowGlyphIds);

        if (children != null && !children.isEmpty()) {

            List<Rotation3f> childRotations = GlyphMath.getChildRotations(children.size(), parentGlyph.getScale(),
                    parentGlyph.getRotation().z());

            float scaleAmount = parentGlyph.getScale() * SCALE_MULTIPLIER;
            if (children.size() == 1) {
                scaleAmount = parentGlyph.getScale() * SCALE_SINGLE_GLYPH;
            }

            for (int i = 0; i < children.size(); i++) {
                Ref<EntityStore> childRef = children.get(i);
                GlyphComponent child = accessor.getComponent(childRef, GlyphComponent.getComponentType());
                Rotation3f childRotation = childRotations.get(i);

                if (child == null) {
                    continue;
                }

                if (styledGlyphs.contains(child.getId())) {
                    continue;
                }
                styledGlyphs.add(child.getId());

                child.setScale(scaleAmount);
                child.setRotation(childRotation);
                child.setVisualOffset(GlyphMath.toMountOffset(childRotation, parentGlyph.getRotation()));

                GlyphStyler.updateScale(accessor, childRef, child.getScale());
                GlyphStyler.updateMountPosition(accessor, child, child.getOffset());

                UpdateGlyphTree(accessor, hexComponent, child, styledGlyphs);
            }
        }
    }
}
