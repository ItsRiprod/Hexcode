package com.riprod.hexcode.builtin.hexCore.glyphs.deprecated;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.utils.LogScopes;

public class DeprecatedGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    public static final String TYPE = "Deprecated";

    public DeprecatedGlyph() {
        this.isDeprecated = true;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        LOGGER.atWarning().log("Glyph %s was removed and no longer executes", glyph.getGlyphId());
        HexExecuter.endBranch(hexContext);
    }
}
