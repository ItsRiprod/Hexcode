package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onCast;

import com.riprod.hexcode.builtin.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;

public class OnCastGlyph extends AbstractTriggerGlyph {

    public static final String TYPE = "OnCast";

    @Override
    public String triggerKey() {
        return TriggerKey.CAST;
    }
}
