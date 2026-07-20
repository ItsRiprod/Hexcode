package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.number;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class NumberValue implements GlyphHandler {

    public static final String ID = "Number";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(NumberConfig.class, NumberConfig.CODEC);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        NumberConfig config = getConfig(NumberConfig.class, asset);
        if (config == null) {
            config = NumberConfig.DEFAULTS;
        }
        return new NumberVar(config.getValue());
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        NumberConfig config = getConfig(NumberConfig.class, asset);
        if (config == null) {
            config = NumberConfig.DEFAULTS;
        }
        hexContext.setDefaultVariable(new NumberVar(config.getValue()));
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
