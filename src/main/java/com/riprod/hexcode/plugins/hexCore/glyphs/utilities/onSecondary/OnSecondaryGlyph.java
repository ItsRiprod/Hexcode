package com.riprod.hexcode.plugins.hexCore.glyphs.utilities.onSecondary;

import com.riprod.hexcode.plugins.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.plugins.imbued.triggers.TriggerKey;

public class OnSecondaryGlyph extends AbstractTriggerGlyph {

    public static final String ID = "OnSecondary";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String triggerKey() {
        return TriggerKey.SECONDARY;
    }
}
