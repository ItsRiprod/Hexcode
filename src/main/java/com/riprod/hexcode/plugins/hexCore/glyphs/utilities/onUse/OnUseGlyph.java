package com.riprod.hexcode.plugins.hexCore.glyphs.utilities.onUse;

import com.riprod.hexcode.plugins.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.plugins.imbued.triggers.TriggerKey;

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
