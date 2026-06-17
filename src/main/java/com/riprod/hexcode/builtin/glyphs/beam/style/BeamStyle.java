package com.riprod.hexcode.builtin.glyphs.beam.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class BeamStyle {

    private static final String GLYPH_ID = "Beam";
    private static final Vector3f DEFAULT_COLOR = new Vector3f(0.4f, 0.2f, 0.67f);
    private static final double LINE_THICKNESS = 0.02;
    private static final float LINE_DURATION = 0.4f;

    private BeamStyle() {
    }

    public enum HitType {
        ENTITY, BLOCK, MISS
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void render(Vector3d origin, Vector3d endPoint, Vector3f rotation, HitType hitType,
            HexContext ctx, ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        Vector3f beamColor = resolveColor(overrides);

        World world = accessor.getExternalData().getWorld();
        VfxUtil.spawnPrimaryDirected(overrides, asset(), origin, new Rotation3f(rotation.x, rotation.y, rotation.z), accessor);
        VfxUtil.line(accessor, world, origin, endPoint, beamColor, LINE_THICKNESS, LINE_DURATION, DebugUtils.FLAG_FADE);

        if (hitType != HitType.MISS) {
            VfxUtil.spawnSecondary(overrides, asset(), endPoint, accessor);
        }
    }

    private static Vector3f resolveColor(HexStyleAsset overrides) {
        Color c = overrides != null ? overrides.getPrimaryColor() : null;
        if (c == null) {
            HexStyleAsset glyphStyle = asset() != null ? asset().getStyle() : null;
            c = glyphStyle != null ? glyphStyle.getPrimaryColor() : null;
        }
        return c != null ? HexColors.toVector3f(c) : DEFAULT_COLOR;
    }
}
