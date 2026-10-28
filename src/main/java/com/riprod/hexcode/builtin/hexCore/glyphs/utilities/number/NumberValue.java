package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.number;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class NumberValue extends GlyphAsset {

    public static final String TYPE = "Number";

    private double value = 0.0;

    public static final BuilderCodec<NumberValue> CODEC = BuilderCodec
            .builder(NumberValue.class, NumberValue::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Value", Codec.DOUBLE, true),
                    (c, v) -> c.value = v, c -> c.value)
            .add()
            .build();

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        return new NumberVar(value);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        hexContext.setDefaultVariable(new NumberVar(value));
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
