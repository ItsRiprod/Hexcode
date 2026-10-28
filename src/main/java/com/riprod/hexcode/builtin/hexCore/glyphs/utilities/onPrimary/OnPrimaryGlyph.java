package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.onPrimary;

import com.riprod.hexcode.builtin.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;

public class OnPrimaryGlyph extends AbstractTriggerGlyph {

    public static final String TYPE = "OnPrimary";

    @Override
    public String triggerKey() {
        return TriggerKey.PRIMARY;
    }
}
