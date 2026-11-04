package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.query;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class QueryGlyph extends GlyphAsset {

    public static final String TYPE = "Query";

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getOwnVariable(glyph.getId());
        if (self != null) {
            return self;
        }
        return compute(glyph, hexContext);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar result = compute(glyph, hexContext);

        if (result != null) {
            glyph.writeOutput(result, hexContext);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private static HexVar compute(Glyph glyph, HexContext hexContext) {
        Slot reference = glyph.getSlot(QueryGlyphSlots.REFERENCE);
        // an unwired Input slot resolves to the context default variable rather than
        // null,
        // so emptiness has to be read off the link itself
        boolean referenceWired = reference != null && reference.getFirstLink() != null;

        if (!referenceWired) {
            return new NumberVar(readSpellStat(readTrilean(glyph, hexContext), hexContext));
        }

        int trilean = readTrilean(glyph, hexContext);
        HexVar value = glyph.readSlot(QueryGlyphSlots.REFERENCE, hexContext);
        var accessor = hexContext.getAccessor();

        if (value instanceof EntityVar entityVar) {
            return new NumberVar(readEntityStat(entityVar, trilean, hexContext));
        }
        if (value instanceof RotationVar) {
            return new NumberVar(HexVarUtil.axis(value.rotation(accessor), trilean + 1));
        }
        // positions, blocks and colors all reach their three channels through
        // toPosition;
        // numbers short-circuit inside the helper and ignore the axis
        return new NumberVar(HexVarUtil.axis(value.position(accessor), trilean + 1));
    }

    private static int readTrilean(Glyph glyph, HexContext hexContext) {
        HexVar value = glyph.readSlot(QueryGlyphSlots.TRILEAN, hexContext);
        long rounded = Math.round(HexVarUtil.numberOrDefault(value, 0.0));
        return (int) Math.max(-1L, Math.min(1L, rounded));
    }

    private static double readSpellStat(int trilean, HexContext hexContext) {
        VolatilityComponent stats = hexContext.volatility();

        return switch (trilean) {
            case -1 -> stats.getInitial();
            case 1 -> stats.getSpent();
            default -> stats.getCurrent();
        };
    }

    private static double readEntityStat(EntityVar entityVar, int trilean, HexContext hexContext) {
        var accessor = hexContext.getAccessor();

        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid())
            return 0.0;

        EntityStatMap statMap = accessor.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null)
            return 0.0;

        int index = switch (trilean) {
            case -1 -> DefaultEntityStatTypes.getStamina();
            case 1 -> DefaultEntityStatTypes.getHealth();
            default -> DefaultEntityStatTypes.getMana();
        };

        EntityStatValue stat = statMap.get(index);
        return stat == null ? 0.0 : stat.get();
    }
}
