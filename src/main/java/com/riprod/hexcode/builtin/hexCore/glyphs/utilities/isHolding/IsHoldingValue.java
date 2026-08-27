package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.isHolding;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class IsHoldingValue implements GlyphHandler {

    public static final String ID = "IsHolding";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> casterRef = hexContext.getCasterRef(hexContext.getAccessor());
        if (casterRef == null || !casterRef.isValid() || hexContext.getAccessor() == null) {
            return new NumberVar(0.0);
        }
        EntityStatMap statMap = hexContext.getAccessor().getComponent(
                casterRef, EntityStatMap.getComponentType());
        EntityStatValue stat = statMap != null
                ? statMap.get(HexcodeEntityStatTypes.getIsHolding()) : null;
        boolean holding = stat != null && stat.get() >= 1f;
        return new NumberVar(holding ? 1.0 : 0.0);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
