package com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.style;

import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.IlluminateConfig;
import com.riprod.hexcode.core.common.hexes.component.HexColors;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public final class IlluminateStyle {

    private IlluminateStyle() {
    }

    public static Color resolveColor(HexContext hexContext, GlyphAsset asset, IlluminateConfig config) {
        HexStyleAsset override = hexContext != null ? hexContext.getStyle() : null;
        Color color = override != null ? override.getPrimaryColor() : null;
        if (color == null) {
            HexStyleAsset glyphStyle = asset != null ? asset.getStyle() : null;
            color = glyphStyle != null ? glyphStyle.getPrimaryColor() : null;
        }
        if (color == null) {
            color = config.getDefaultColor();
        }
        return color;
    }

    public static ColorLight toColorLight(Color color, int level) {
        int r = color.red & 0xFF;
        int g = color.green & 0xFF;
        int b = color.blue & 0xFF;
        int max = Math.max(r, Math.max(g, b));
        if (level <= 0 || max == 0) {
            return new ColorLight((byte) level, (byte) 0, (byte) 0, (byte) 0);
        }
        return new ColorLight((byte) level,
                (byte) (r * level / max),
                (byte) (g * level / max),
                (byte) (b * level / max));
    }

    public static Vector3f toVector3f(Color color) {
        return HexColors.toVector3f(color);
    }
}
