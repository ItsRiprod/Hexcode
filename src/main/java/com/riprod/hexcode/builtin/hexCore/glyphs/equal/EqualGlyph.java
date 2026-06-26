package com.riprod.hexcode.builtin.hexCore.glyphs.equal;

import java.util.Arrays;
import java.util.List;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexCompareUtil;

public class EqualGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Equal";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(EqualGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(EqualGlyphSlots.B, hexContext);

        if (b == null) {
            assign(glyph, hexContext, a);
            return;
        }

        branch(glyph, hexContext, a, b);
    }

    private void assign(Glyph glyph, HexContext hexContext, HexVar value) {
        if (value != null) {
            glyph.writeOutput(value, hexContext);
        }
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void branch(Glyph glyph, HexContext hexContext, HexVar a, HexVar b) {
        boolean result = HexCompareUtil.isEqual(a, b);

        List<String> next = glyph.getNextLinks();
        Slot greaterSlot = glyph.getSlot(EqualGlyphSlots.GREATER);
        List<String> greaterLinks = Arrays.asList(greaterSlot.getLinks());
        Slot lessSlot = glyph.getSlot(EqualGlyphSlots.LESS);
        List<String> lessLinks = Arrays.asList(lessSlot.getLinks());

        if (result) {
            if (!next.isEmpty()) {
                HexExecuter.continueExecution(next, hexContext);
            }
        } else if (HexCompareUtil.isGreater(a, b)) {
            if (!greaterLinks.isEmpty()) {
                HexExecuter.continueExecution(greaterLinks, hexContext);
            }
        } else if (HexCompareUtil.isLess(a, b)) {
            if (!lessLinks.isEmpty()) {
                HexExecuter.continueExecution(lessLinks, hexContext);
            }
        }
    }
}
