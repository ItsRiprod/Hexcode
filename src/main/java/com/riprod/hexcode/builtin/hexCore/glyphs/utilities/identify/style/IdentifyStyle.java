package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.style;

import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify.IdentifyConfig;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public final class IdentifyStyle {

    private IdentifyStyle() {
    }

    public static Vector3f resolveColor(HexContext hexContext, GlyphAsset asset, IdentifyConfig config) {
        HexStyleAsset override = hexContext != null ? hexContext.getStyle() : null;
        Color color = override != null ? override.getPrimaryColor() : null;
        if (color == null) {
            HexStyleAsset glyphStyle = asset != null ? asset.getStyle() : null;
            color = glyphStyle != null ? glyphStyle.getPrimaryColor() : null;
        }
        if (color == null) {
            color = config.getDefaultColor();
        }
        return HexColors.toVector3f(color);
    }
}
