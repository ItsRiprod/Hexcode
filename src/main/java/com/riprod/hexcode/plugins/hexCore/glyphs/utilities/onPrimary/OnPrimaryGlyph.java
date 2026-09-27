package com.riprod.hexcode.plugins.hexCore.glyphs.utilities.onPrimary;

import com.riprod.hexcode.plugins.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.plugins.imbued.triggers.TriggerKey;

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
