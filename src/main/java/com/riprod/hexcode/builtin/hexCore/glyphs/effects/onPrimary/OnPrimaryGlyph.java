package com.riprod.hexcode.builtin.hexCore.glyphs.effects.onPrimary;

import com.riprod.hexcode.builtin.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;

public class OnPrimaryGlyph extends AbstractTriggerGlyph {

    public static final String ID = "OnPrimary";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String triggerKey() {
        return TriggerKey.PRIMARY;
    }
}
