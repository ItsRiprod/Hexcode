package com.riprod.hexcode.builtin.hexCore.glyphs.area.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;
import java.util.List;

import static com.hypixel.hytale.server.core.modules.debug.DebugUtils.COLOR_WHITE;

public class AreaStyle {

    private static final String GLYPH_ID = "Area";
    private static final Vector3f DEFAULT_COLOR = new Vector3f(0.3f, 0.6f, 0.9f);
    private static final float SPHERE_DURATION = 0.5f;

    private AreaStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void render(Vector3d center, double radius, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        Vector3f color = resolveColor(overrides);

        World world = accessor.getExternalData().getWorld();

        Matrix4d matrix = new Matrix4d();
        matrix.identity();
        matrix.translate(center.x, center.y, center.z);
        matrix.scale(radius * 2.0, radius * 2.0, radius * 2.0);

//        Matrix4d hull = new Matrix4d();
//        hull.identity();
//        hull.translate(center.x, center.y, center.z);
//        hull.scale(radius * 2.2, radius * 2.2, radius * 2.2);

        int flags = DebugUtils.FLAG_FADE | DebugUtils.FLAG_NO_WIREFRAME;
        DebugUtils.add(world, DebugShape.Sphere, matrix, color, SPHERE_DURATION, flags);
//        DebugUtils.add(world, DebugShape.Sphere, hull, COLOR_WHITE, SPHERE_DURATION, flags);


        VfxUtil.spawnPrimary(overrides, asset(), center, accessor);
    }

    public static void renderHit(Vector3d pos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        renderHit(pos, ctx, accessor, null);
    }

    public static void renderHit(Vector3d pos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor, List<Ref<EntityStore>> recipients) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), pos, accessor, recipients);
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
