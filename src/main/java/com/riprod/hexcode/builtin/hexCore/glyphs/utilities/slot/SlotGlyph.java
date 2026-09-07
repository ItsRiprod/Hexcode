package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.slot;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean.TrileanSlot;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public class SlotGlyph implements GlyphHandler {

    public static final String ID = "Slot";

    public static final int MODE_NEXT = -1;
    public static final int MODE_TRILEAN = 0;
    public static final int MODE_INPUT = 1;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        var port = glyph.getSlot(SlotGlyphSlots.PORT);
        if (port != null && (port.getFirstLink() != null || port.inlineValue() != null)) {
            return glyph.readSlot(SlotGlyphSlots.PORT, hexContext);
        }
        return glyph.readSlot(SlotGlyphSlots.DEFAULT, hexContext);
    }

    public static int mode(Glyph glyph) {
        if (glyph.getSlot(SlotGlyphSlots.MODE) instanceof TrileanSlot bool && bool.getState() != null) {
            return bool.getState().value();
        }
        var asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        var config = asset != null ? asset.getSlot(SlotGlyphSlots.MODE) : null;
        var fallback = config != null ? config.getDefaultValue() : null;
        return fallback != null ? (int) Math.round(fallback) : MODE_NEXT;
    }

    public static boolean isSlotGlyph(Glyph glyph) {
        var asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        return asset != null && ID.equals(asset.getHandler());
    }
}
