package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import com.riprod.hexcode.builtin.hexCore.scene.GlyphStyler;
import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.utils.CreateGlyph;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.utils.GlyphMath;

public class GlyphSpawner {
    public static void spawnGlyphs(CommandBuffer<EntityStore> accessor, HexComponent hex, GlyphComponent glyph,
            Vector3d parentPos, Rotation3f parentRot) {
            spawnGlyphs(accessor, hex, glyph, parentPos, parentRot, null);
    }

    public static void spawnGlyphs(CommandBuffer<EntityStore> accessor, HexComponent hex, GlyphComponent glyph,
            Vector3d parentPos, Rotation3f parentRot, Ref<EntityStore> playerRef) {

        Ref<EntityStore> glyphRef = CreateGlyph.createGlyph(accessor, glyph, parentPos, parentRot, playerRef);
        glyph.setSelfRef(glyphRef);
        hex.addChildGlyphRef(glyph.getId(), glyphRef);

        List<Glyph> children = hex.getGlyphs(glyph.getFlowLinks());

        List<Rotation3f> childRotations = GlyphMath.getChildRotations(children.size(), glyph.getScale(), glyph.getRotation().z());

        for (int i = 0; i < children.size(); i++) {

            Glyph childGlyph = children.get(i);
            Rotation3f childRotation = childRotations.get(i);
            if (hex.getChildGlyphRef(childGlyph.getId()) != null) {
                continue;
            }

            GlyphComponent childGlyphComponent = new GlyphComponent(childGlyph);

            childGlyphComponent.setRotation(childRotation);
            childGlyphComponent.setVisualOffset(GlyphMath.toMountOffset(childRotation, glyph.getRotation()));

            if (children.size() == 1) {
                childGlyphComponent.setScale(glyph.getScale() * GlyphStyler.SCALE_SINGLE_GLYPH);
            } else {
                childGlyphComponent.setScale(glyph.getScale() * GlyphStyler.SCALE_MULTIPLIER);
            }

            childGlyphComponent.setParentRef(glyph.getSelfRef());
            childGlyphComponent.setHexRef(hex.getSelfRef());

            spawnGlyphs(accessor, hex, childGlyphComponent, parentPos, parentRot, playerRef);
        }
    }
}
