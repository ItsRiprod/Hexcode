package com.riprod.hexcode.core.common.positioning.layout;

import java.util.List;

import javax.annotation.Nonnull;

import com.riprod.hexcode.config.HexcodeConfig;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public interface HexLayout {

    @Nonnull
    String getLayoutId();

    @Nonnull
    List<Placement> layout(@Nonnull Hex hex, @Nonnull LayoutParams params);

    default float rootScale(@Nonnull Hex hex) {
        var config = HexcodeConfig.get();
        var n = hex.getGlyphs().stream().filter(g -> g != null).count();
        var max = config.getRootScaleMaxGrowth();
        if (max <= 0f) {
            return 1f;
        }
        return 1f + max * (1f - (float) Math.exp(-n * config.getRootScalePerGlyph() / max));
    }
}
