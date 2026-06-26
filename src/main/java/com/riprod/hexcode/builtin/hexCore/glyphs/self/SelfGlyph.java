package com.riprod.hexcode.builtin.hexCore.glyphs.self;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class SelfGlyph implements GlyphHandler {
    @Override
public String getId() { return ID; };

public static final String ID = "Self";

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> playerRef = hexContext.getCasterRef();
        if (playerRef == null || !playerRef.isValid()) return null;

        UUIDComponent uuidComponent = hexContext.getAccessor().getComponent(playerRef, UUIDComponent.getComponentType());
        return new EntityVar(EntityVar.createRef(uuidComponent.getUuid(), playerRef));
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar result = compute(glyph, hexContext);

        if (result != null) {
            glyph.writeOutput(result, hexContext);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        return compute(glyph, hexContext);
    }
}
