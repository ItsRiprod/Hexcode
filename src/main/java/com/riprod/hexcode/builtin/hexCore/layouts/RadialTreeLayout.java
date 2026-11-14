package com.riprod.hexcode.builtin.hexCore.layouts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.joml.Vector3f;

import com.riprod.hexcode.config.HexcodeConfig;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.positioning.layout.HexLayout;
import com.riprod.hexcode.core.common.positioning.layout.LayoutParams;
import com.riprod.hexcode.core.common.positioning.layout.Placement;
import com.riprod.hexcode.utils.GlyphMath;

public final class RadialTreeLayout implements HexLayout {

    public static final String ID = "radial";

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
        var out = new ArrayList<Placement>();
        Set<String> placed = new HashSet<>();
        var rootPlacement = new Placement(root, null, new Vector3f(), params.rootScale(), 0f);
        out.add(rootPlacement);
        placed.add(root.getId());
        placeChildren(hex, rootPlacement, params.distance(), params, HexcodeConfig.get(), placed, out);
        return out;
    }

    private static void placeChildren(Hex hex, Placement parent, float parentDistance, LayoutParams params,
            HexcodeConfig config, Set<String> placed, List<Placement> out) {
        var children = hex.getGlyphs(parent.glyph().getFlowLinks());
        if (children.isEmpty()) {
            return;
        }
        var only = children.size() == 1;
        var childScale = parent.scale() * (only ? config.getOnlyChildScale() : config.getChildScale());
        var childDistance = only ? 0f : parentDistance;
        var spread = GlyphMath.getSelectionRadius(parent.scale()) * config.getChildSpread();

        for (var i = 0; i < children.size(); i++) {
            var child = children.get(i);
            if (!placed.add(child.getId())) {
                continue;
            }
            var offset = new Vector3f();
            if (!only) {
                var theta = 2 * Math.PI * i / children.size();
                var dPitch = spread * (float) Math.cos(theta);
                var dYaw = spread * (float) Math.sin(theta);
                offset.set(-dYaw * parentDistance, dPitch * parentDistance, 0f);
            }
            offset.z += childScale * params.heightPerScale();
            var placement = new Placement(child, parent.glyph().getId(), offset, childScale, 0f);
            out.add(placement);
            placeChildren(hex, placement, childDistance, params, config, placed, out);
        }
    }
}
