package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onUse;

import com.riprod.hexcode.builtin.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;

public class OnUseGlyph extends AbstractTriggerGlyph {

    public static final String TYPE = "OnUse";

    @Override
    public String triggerKey() {
        return TriggerKey.USE;
    }
}
