package com.riprod.hexcode.builtin.hexCore.layouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.riprod.hexcode.config.HexcodeConfig;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.positioning.layout.HexLayout;
import com.riprod.hexcode.core.common.positioning.layout.LayoutParams;
import com.riprod.hexcode.core.common.positioning.layout.Placement;

public final class AuthoredFlatLayout implements HexLayout {

    public static final String ID = "authored";

    private static final float MIN_EXTENT = 1e-4f;

    @Nonnull
    @Override
    public String getLayoutId() {
        return ID;
    }

    @Nonnull
    @Override
    public List<Placement> layout(@Nonnull Hex hex, @Nonnull LayoutParams params) {
        var firstId = hex.getFirstGlyphId();
        var root = firstId != null ? hex.get(firstId) : null;
        if (root == null) {
            return List.of();
        }
        var config = HexcodeConfig.get();

        var order = new ArrayList<Glyph>();
        var depths = new HashMap<String, Integer>();
        collect(hex, root, 0, order, depths, new HashSet<>());

        var min = new Vector2f(Float.MAX_VALUE);
        var max = new Vector2f(-Float.MAX_VALUE);
        var layers = new TreeSet<Float>();
        for (var glyph : order) {
            var plane = plane(glyph);
            min.min(plane);
            max.max(plane);
            layers.add(glyph.getPosition().y);
        }
        var center = new Vector2f(min).add(max).mul(0.5f);
        var extent = 0f;
        for (var glyph : order) {
            extent = Math.max(extent, plane(glyph).distance(center));
        }
        var k = extent < MIN_EXTENT ? 0f : config.getAuthoredRadius() * params.rootScale() / extent;
        var layerIndex = new HashMap<Float, Integer>();
        for (var y : layers) {
            layerIndex.put(y, layerIndex.size());
        }

        var rootPlane = plane(root);
        var rootSpin = spin(root);
        var rootLayer = layerIndex.get(root.getPosition().y);
        var cos = (float) Math.cos(-rootSpin);
        var sin = (float) Math.sin(-rootSpin);

        var out = new ArrayList<Placement>(order.size());
        for (var glyph : order) {
            var scale = params.rootScale() * (float) Math.pow(config.getAuthoredDepthFalloff(), depths.get(glyph.getId()));
            var layer = layerIndex.get(glyph.getPosition().y);
            if (glyph == root) {
                var offset = new Vector2f(rootPlane).sub(center).mul(k);
                out.add(new Placement(glyph, null, new Vector3f(offset.x, offset.y, layer * config.getAuthoredLayerSpacing()),
                        scale, rootSpin));
                continue;
            }
            // mounted to the root, so the offset and spin are in the root's (spun) frame
            var d = plane(glyph).sub(rootPlane).mul(k);
            var offset = new Vector3f(d.x * cos - d.y * sin, d.x * sin + d.y * cos,
                    (layer - rootLayer) * config.getAuthoredLayerSpacing());
            out.add(new Placement(glyph, root.getId(), offset, scale, spin(glyph) - rootSpin));
        }
        return out;
    }

    private static void collect(Hex hex, Glyph glyph, int depth, List<Glyph> order, Map<String, Integer> depths,
            Set<String> seen) {
        if (!seen.add(glyph.getId())) {
            return;
        }
        order.add(glyph);
        depths.put(glyph.getId(), depth);
        for (var child : hex.getGlyphs(glyph.getFlowLinks())) {
            collect(hex, child, depth + 1, order, depths, seen);
        }
    }

    private static Vector2f plane(Glyph glyph) {
        var p = glyph.getPosition();
        return new Vector2f(p.x, -p.z);
    }

    private static float spin(Glyph glyph) {
        return glyph.getRotation().y;
    }
}
