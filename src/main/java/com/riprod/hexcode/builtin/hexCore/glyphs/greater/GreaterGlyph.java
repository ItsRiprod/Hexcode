package com.riprod.hexcode.builtin.hexCore.glyphs.greater;

import java.util.List;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.utils.HexCompareUtil;

public class GreaterGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Greater";

    public HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(GreaterGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(GreaterGlyphSlots.B, hexContext);
        boolean result = HexCompareUtil.isGreater(a, b);
        return new NumberVar(result ? 1 : 0);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getVariable(glyph.getId());

        if (self != null) {
            return self;
        }
        return compute(glyph, hexContext);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar resultVar = compute(glyph, hexContext);

        if (resultVar != null) {
            glyph.writeOutput(resultVar, hexContext);
        }

        List<String> next = glyph.getNextLinks();
        if (resultVar instanceof NumberVar numberVar && numberVar.getValue() != null && numberVar.getValue() != 0) {
            if (!next.isEmpty()) {
                HexExecuter.continueExecution(List.of(next.get(0)), hexContext);
            }
        } else {
            if (next.size() > 1) {
                HexExecuter.continueExecution(next.subList(1, next.size()), hexContext);
            }
        }
    }
}
