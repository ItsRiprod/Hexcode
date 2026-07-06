package com.riprod.hexcode.builtin.hexCore.glyphs.effects.onCast;

import com.riprod.hexcode.builtin.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;

public class OnCastGlyph extends AbstractTriggerGlyph {

    public static final String ID = "OnCast";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String triggerKey() {
        return TriggerKey.CAST;
    }
}
