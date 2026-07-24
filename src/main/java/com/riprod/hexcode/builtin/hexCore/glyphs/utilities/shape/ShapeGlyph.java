package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.shape;

import javax.annotation.Nullable;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public class ShapeGlyph implements GlyphHandler {
    public static final String ID = "Shape";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexStyleAsset linkedStyle = resolveLinkedGlyphStyle(glyph, hexContext);
        if (linkedStyle != null) hexContext.mutableStyle().applyOverride(linkedStyle);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private static @Nullable HexStyleAsset resolveLinkedGlyphStyle(Glyph glyph, HexContext hexContext) {
        Slot shapeSlot = glyph.getSlots().get(ShapeGlyphSlots.SHAPE);
        if (shapeSlot == null) return null;
        String linkedId = shapeSlot.getFirstLink();
        if (linkedId == null) return null;
        Glyph linked = hexContext.getGlyph(linkedId);
        if (linked == null) return null;
        GlyphAsset linkedAsset = GlyphAsset.getAssetMap().getAsset(linked.getGlyphId());
        return linkedAsset != null ? linkedAsset.getStyle() : null;
    }
}
