package com.riprod.hexcode.plugins.hexCore.glyphs.utilities.onCast;

import com.riprod.hexcode.plugins.imbued.triggers.AbstractTriggerGlyph;
import com.riprod.hexcode.plugins.imbued.triggers.TriggerKey;

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
