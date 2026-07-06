package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.variable;

import java.util.List;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class VariableValue implements GlyphHandler {

    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Variable";

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {

        HexVar self = hexContext.getVariable(glyph.getId());

        if (self != null) {
            return self;
        }

        HexVar input = glyph.readSlot(VariableValueSlots.INPUT, hexContext);

        if (input == null) {
            return null;
        }

        HexVar variableValue = hexContext.getVariable(input.toString());
        return variableValue;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar input = glyph.readSlot(VariableValueSlots.INPUT, hexContext);
        List<HexVar> outputs = glyph.readSlotAll(VariableValueSlots.OUTPUT, hexContext);

        if (outputs != null && outputs.size() > 0) {
            for (HexVar output : outputs) {
                NumberVar numberVar = new NumberVar(output.toScalar());
                hexContext.setVariable(numberVar.toString(), input);
            }
        }

        hexContext.setDefaultVariable(input);
        hexContext.setVariable(glyph.getId(), input);
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
