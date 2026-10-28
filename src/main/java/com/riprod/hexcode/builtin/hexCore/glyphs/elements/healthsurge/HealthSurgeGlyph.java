package com.riprod.hexcode.builtin.hexCore.glyphs.elements.healthsurge;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphAsset;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class HealthSurgeGlyph extends ElementGlyphAsset {

    public static final String TYPE = "HealthSurge";

    public static final BuilderCodec<HealthSurgeGlyph> CODEC = BuilderCodec
            .builder(HealthSurgeGlyph.class, HealthSurgeGlyph::new, ElementGlyphAsset.ABSTRACT_CODEC)
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Health Surge must target an entity");
            return;
        }

        float affinity = ElementSupport.affinityFactor(hexContext, affinityStat, affinityScale);
        float limit = ElementSupport.resourceLimit(glyph, this, hexContext);
        float heal = ElementSupport.consumeResource(hexContext, glyph, resource, limit)
                * efficiency * affinity;

        EntityStatMap statMap = hexContext.getAccessor().getComponent(
                target, EntityStatMap.getComponentType());
        if (statMap != null) {
            statMap.addStatValue(DefaultEntityStatTypes.getHealth(), heal);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
