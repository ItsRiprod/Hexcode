package com.riprod.hexcode.builtin.hexCore.glyphs.effects.chaos;

import java.util.concurrent.ThreadLocalRandom;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class ChaosGlyph implements GlyphHandler {
    public static final String ID = "Chaos";

    @Override
    public String getId() {
        return ID;
    }

    private double roll(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        HexVar minVar = glyph.readSlot(ChaosGlyphSlots.MIN, hexContext);
        HexVar maxVar = glyph.readSlot(ChaosGlyphSlots.MAX, hexContext);
        double min = HexVarUtil.numberOrSlotDefault(minVar, asset.getSlot(ChaosGlyphSlots.MIN));
        double max = HexVarUtil.numberOrSlotDefault(maxVar, asset.getSlot(ChaosGlyphSlots.MAX));
        if (min == max) return min;
        if (min > max) {
            double tmp = min;
            min = max;
            max = tmp;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        glyph.writeOutput(new NumberVar(roll(glyph, hexContext)), hexContext);
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        return new NumberVar(roll(glyph, hexContext));
    }
}
