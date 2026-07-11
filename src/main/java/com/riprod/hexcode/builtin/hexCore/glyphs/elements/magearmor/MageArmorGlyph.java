package com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor.component.MagicHealthComponent;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public class MageArmorGlyph implements GlyphHandler {

    public static final String ID = "MageArmor";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(MageArmorConfig.class, MageArmorConfig.CODEC);
    }

    @Override
    public float getComplexity(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return 0f;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Mage Armor must target an entity");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        MageArmorConfig config = getConfig(MageArmorConfig.class, asset);
        if (config == null) config = MageArmorConfig.DEFAULTS;

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        EntityStatMap statMap = accessor.getComponent(target, EntityStatMap.getComponentType());
        if (statMap == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Mage Armor target has no stats");
            return;
        }

        float affinity = ElementSupport.affinityFactor(
                hexContext, config.getAffinityStat(), config.getAffinityScale());
        float complexity = hexContext.consumeComplexity();
        float pool = complexity * config.getEfficiency() * affinity;
        float duration = pool * config.getDurationPerComplexity();

        int statIndex = EntityStatType.getAssetMap().getIndex(MagicHealthComponent.STAT_ID);
        if (statIndex == Integer.MIN_VALUE) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Mage Armor stat missing");
            return;
        }
        statMap.addStatValue(statIndex, pool);

        String effectId = config.getStatusEffect();
        accessor.putComponent(target, MagicHealthComponent.getComponentType(),
                new MagicHealthComponent(effectId));
        ElementSupport.applyStatus(target, accessor, effectId, duration);

        MageArmorState existing = ConstructStateUtil.findState(
                accessor, target, MageArmorGlyph.ID, MageArmorState.class);
        if (existing != null) {
            existing.refresh(duration, glyph.getNextLinks());
        } else {
            MageArmorState state = new MageArmorState(effectId, duration, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, target, hexContext, glyph, MageArmorGlyph.ID, state);
        }
    }
}
