package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onSecondary;

import com.riprod.hexcode.builtin.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;

public class OnSecondaryGlyph extends AbstractTriggerGlyph {

    public static final String TYPE = "OnSecondary";

    @Override
    public String triggerKey() {
        return TriggerKey.SECONDARY;
    }
}
