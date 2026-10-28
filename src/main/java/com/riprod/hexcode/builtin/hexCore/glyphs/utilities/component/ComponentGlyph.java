package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.component;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class ComponentGlyph extends GlyphAsset {

    public static final String TYPE = "Component";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                "component payload unresolved");
    }
}
