package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.sound;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public class SoundGlyph implements GlyphHandler {
    public static final String ID = "Sound";

    // authored on the same 0-512 axis as Color's channels, where unity is half scale
    private static final double VOLUME_UNITY = 256.0;
    private static final double VOLUME_INPUT_MAX = 512.0;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Slot slot = glyph.getSlots().get(SoundGlyphSlots.VOLUME);
        if (slot == null || slot.getFirstLink() == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        HexVar input = glyph.readSlot(SoundGlyphSlots.VOLUME, hexContext);
        double raw = input == null || input.toScalar() == null ? VOLUME_UNITY : input.toScalar();
        double clamped = raw < 0.0 ? 0.0 : Math.min(raw, VOLUME_INPUT_MAX);

        HexStyleAsset style = hexContext.mutableStyle();
        style.setVolume((float) (clamped / VOLUME_UNITY));

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
