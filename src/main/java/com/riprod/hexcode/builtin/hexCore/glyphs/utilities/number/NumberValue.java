package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.number;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;

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
        HexExecuter.continueExecution(collectLikeNumberedLinks(glyph, hexContext), hexContext);
    }

    private static List<String> collectLikeNumberedLinks(Glyph glyph, HexContext hexContext) {
        Hex hex = hexContext.getHex();
        if (hex == null) {
            return glyph.getNextLinks();
        }

        List<Glyph> likeNumbered = new ArrayList<>();
        for (Glyph candidate : hex.getGlyphs()) {
            if (glyph.getGlyphId().equals(candidate.getGlyphId())) {
                likeNumbered.add(candidate);
            }
        }
        // lowest glyph fires first; sort is stable so equal heights keep load order
        likeNumbered.sort(Comparator.comparingDouble(g -> g.getPosition().y));

        List<String> links = new ArrayList<>();
        for (Glyph match : likeNumbered) {
            links.addAll(match.getNextLinks());
        }
        return links;
    }
}
