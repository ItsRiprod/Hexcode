package com.riprod.hexcode.core.common.positioning.utils;

import java.util.LinkedHashMap;

import javax.annotation.Nonnull;

import org.joml.Vector3f;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hover.utils.GlyphStyler;
import com.riprod.hexcode.core.common.positioning.layout.HexLayoutState;
import com.riprod.hexcode.core.common.positioning.layout.LayoutParams;
import com.riprod.hexcode.core.common.positioning.layout.Placement;

public final class HexRelayout {

    private HexRelayout() {
    }

    public static void relayout(@Nonnull CommandBuffer<EntityStore> accessor, @Nonnull HexComponent hex,
            @Nonnull GlyphComponent rootGlyph) {
        var state = hex.getLayoutState();
        if (state == null) {
            return;
        }
        var layout = state.layout();
        var old = state.params();
        var params = new LayoutParams(layout.rootScale(hex.getHex()), old.distance(), old.heightPerScale());
        rootGlyph.setScale(params.rootScale());
        hex.setScale(params.rootScale());

        var placements = new LinkedHashMap<String, Placement>();
        for (var placement : layout.layout(hex.getHex(), params)) {
            placements.put(placement.glyph().getId(), placement);
        }
        var spins = HexEntities.accumulatedSpins(placements);
        var facing = new Rotation3f(hex.getRotation().x, hex.getRotation().y, 0f);
        for (var placement : placements.values()) {
            var childRef = hex.getChildGlyphRef(placement.glyph().getId());
            if (childRef == null || !childRef.isValid()) {
                continue;
            }
            var child = accessor.getComponent(childRef, GlyphComponent.getComponentType());
            if (child == null) {
                continue;
            }
            var transform = accessor.getComponent(childRef, TransformComponent.getComponentType());
            var rotation = HexEntities.worldRotation(facing, spins.get(placement.glyph().getId()));
            if (transform != null && !transform.getRotation().equals(rotation)) {
                transform.getRotation().set(rotation);
            }
            if (placement.isRoot()) {
                continue;
            }
            var parentRef = hex.getChildGlyphRef(placement.parentId());
            if (parentRef == null) {
                continue;
            }
            var offset = new Vector3f(placement.offset());
            if (child.getScale() != placement.scale()) {
                child.setScale(placement.scale());
                GlyphStyler.updateScale(accessor, childRef, child.getScale());
            }
            if (!parentRef.equals(child.getParentRef()) || !offset.equals(child.getOffset())) {
                child.setVisualOffset(offset);
                child.setParentRef(parentRef);
                GlyphStyler.updateMountPosition(accessor, child, offset);
            }
        }
        hex.setLayoutState(new HexLayoutState(layout, params, placements));
    }
}
