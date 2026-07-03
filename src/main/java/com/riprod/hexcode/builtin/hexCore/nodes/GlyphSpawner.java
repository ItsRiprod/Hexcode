package com.riprod.hexcode.builtin.hexCore.nodes;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.utils.CreateGlyph;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.builtin.hexCore.scene.GlyphStyler;
import com.riprod.hexcode.utils.GlyphMath;

public class GlyphSpawner {

    private static final float GLYPH_HEIGHT_MULTIPLIER = 0.2f;

    public static void spawnGlyphs(CommandBuffer<EntityStore> accessor, HexComponent hex, GlyphComponent glyph,
            Vector3d parentPos, Rotation3f parentRot, Ref<EntityStore> playerRef) {

        Ref<EntityStore> glyphRef = CreateGlyph.createGlyph(accessor, glyph, parentPos, parentRot, playerRef);
        glyph.setSelfRef(glyphRef);
        hex.addChildGlyphRef(glyph.getId(), glyphRef);

        List<Glyph> children = hex.getGlyphs(glyph.getNext());

        List<Rotation3f> childRotations = GlyphMath.getChildRotations(children.size(), glyph.getScale(),
                glyph.getRotation().z());

        for (int i = 0; i < children.size(); i++) {
            Glyph childGlyph = children.get(i);
            Rotation3f childRotation = childRotations.get(i);
            if (hex.getChildGlyphRef(childGlyph.getId()) != null) {
                continue;
            }

            GlyphComponent childGlyphComponent = new GlyphComponent(childGlyph.clone());

            if (children.size() == 1) {
                childGlyphComponent.setScale(glyph.getScale() * GlyphStyler.SCALE_SINGLE_GLYPH);
            } else {
                childGlyphComponent.setScale(glyph.getScale() * GlyphStyler.SCALE_MULTIPLIER);
            }

            childGlyphComponent.setRotation(childRotation);
            Vector3f offset = GlyphMath.toMountOffset(childRotation, childGlyph.getRotation());
            float yOffset = childGlyphComponent.getScale() * GLYPH_HEIGHT_MULTIPLIER;
            Vector3f scaledOffset = offset.add(0, 0, yOffset);
            childGlyphComponent.setVisualOffset(scaledOffset);

            childGlyphComponent.setParentRef(glyph.getSelfRef());
            childGlyphComponent.setHexRef(hex.getSelfRef());

            spawnGlyphs(accessor, hex, childGlyphComponent, parentPos, parentRot, playerRef);
        }
    }
}
