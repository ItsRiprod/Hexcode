package com.riprod.hexcode.builtin.hexCore.glyphs.effects.onUse;

import com.riprod.hexcode.builtin.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;

public class OnUseGlyph extends AbstractTriggerGlyph {

    public static final String ID = "OnUse";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String triggerKey() {
        return TriggerKey.USE;
    }
}
