package com.riprod.hexcode.builtin.elementalism.glyphs.catalyst;

import javax.annotation.Nullable;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.elementalism.charge.ElementalComponent;
import com.riprod.hexcode.builtin.elementalism.element.ElementAsset;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class CatalystGlyph extends GlyphAsset {

    public static final String TYPE = "Catalyst";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        var elementId = resolveElement(glyph, hexContext);
        if (elementId == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Element must link a single-shape glyph whose shape names an existing element");
            return;
        }
        hexContext.get(ElementalComponent.TYPE).activate(elementId, hexContext);
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        var elementId = resolveElement(glyph, hexContext);
        return new NumberVar(elementId != null ? hexContext.get(ElementalComponent.TYPE).charge(elementId) : 0f);
    }

    @Nullable
    private static String resolveElement(Glyph glyph, HexContext hexContext) {
        var linked = GlyphAsset.of(glyph.linkedGlyph(CatalystGlyphSlots.ELEMENT, hexContext), GlyphAsset.class);
        if (linked == null) {
            return null;
        }
        var shapes = linked.getShapes();
        if (shapes.size() != 1) {
            return null;
        }
        var elementId = shapes.getFirst().getElement();
        return elementId != null && ElementAsset.getAssetMap().getAsset(elementId) != null ? elementId : null;
    }
}
