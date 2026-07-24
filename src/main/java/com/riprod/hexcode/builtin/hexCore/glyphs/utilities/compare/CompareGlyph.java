package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.compare;

import java.util.Arrays;
import java.util.List;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class CompareGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Compare";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        int result = compare(glyph.readSlot(CompareGlyphSlots.A, hexContext),
                glyph.readSlot(CompareGlyphSlots.B, hexContext), hexContext.getAccessor());

        glyph.writeSelfOutput(new NumberVar(result), hexContext);

        Slot slot = glyph.getSlot(slotFor(result));
        List<String> links = slot != null ? Arrays.asList(slot.getLinks()) : List.of();
        if (!links.isEmpty()) {
            HexExecuter.continueExecution(links, hexContext);
        }
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getVariable(glyph.getId());
        if (self != null) {
            return self;
        }

        int result = compare(glyph.readSlot(CompareGlyphSlots.A, hexContext),
                glyph.readSlot(CompareGlyphSlots.B, hexContext), hexContext.getAccessor());
        return glyph.readSlot(slotFor(result), hexContext);
    }

    private int compare(HexVar a, HexVar b, ComponentAccessor<EntityStore> accessor) {
        if (a == null) {
            a = new NumberVar(0.0);
        }
        if (b == null) {
            b = new NumberVar(0.0);
        }

        boolean aEntity = a instanceof EntityVar;
        boolean bEntity = b instanceof EntityVar;
        if (aEntity && bEntity) {
            return a.equalTo(b) ? 0 : 1;
        }
        if (aEntity != bEntity) {
            if (aEntity) {
                a = a.convertTo(b.getClass(), accessor);
            } else {
                b = b.convertTo(a.getClass(), accessor);
            }
        }
        return Integer.signum(a.compareTo(b));
    }

    private String slotFor(int result) {
        if (result > 0) {
            return CompareGlyphSlots.GREATER;
        }
        if (result < 0) {
            return CompareGlyphSlots.LESS;
        }
        return CompareGlyphSlots.EQUAL;
    }
}
