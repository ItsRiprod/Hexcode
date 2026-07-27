package com.riprod.hexcode.builtin.hexCore.glyphs.elements.ignite;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ignite.style.IgniteStyle;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

import java.util.Arrays;

public class IgniteGlyph implements GlyphHandler {

    public static final String ID = "Ignite";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(IgniteConfig.class, IgniteConfig.CODEC);
    }


    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Ignite must target an entity");
            return;
        }

        Ref<EntityStore> caster = hexContext.getCasterRef(hexContext.getAccessor());
        if (!HexProtection.canAffectEntity(hexContext.getAccessor().getExternalData().getWorld(),
                caster, hexContext.getAccessor(), target)) {
            HexProtection.notifyBlocked(caster, hexContext.getAccessor(), getId());
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        IgniteConfig config = getConfig(IgniteConfig.class, asset);
        if (config == null) config = IgniteConfig.DEFAULTS;

        float affinity = ElementSupport.affinityFactor(
                hexContext, config.getAffinityStat(), config.getAffinityScale());
        float limit = ElementSupport.resourceLimit(glyph, asset, hexContext);
        float seconds = ElementSupport.scaledDuration(ElementSupport.consumeResource(hexContext, glyph, config.getResource(), limit),
                config.getEfficiency(), config.getDurationPerComplexity(),
                affinity);

        String effectId = config.getStatusEffect();
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (!ElementSupport.applyStatus(hexContext, target, glyph, effectId, seconds)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Ignite could not apply " + effectId);
            return;
        }

        TransformComponent tc = accessor.getComponent(target, TransformComponent.getComponentType());
        if (tc != null) {
            IgniteStyle.render(tc.getPosition(), hexContext, accessor);
        }

        IgniteState existing = ConstructStateUtil.findState(
                accessor, target, IgniteGlyph.ID, IgniteState.class);
        if (existing != null) {
            existing.setRemainingDuration(seconds);
            existing.setNextGlyphIds(glyph.getNextLinks());
        } else {
            IgniteState state = new IgniteState(seconds, effectId, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, target, hexContext, glyph, IgniteGlyph.ID, state);
        }

        Slot immediate = glyph.getSlot(IgniteGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }
}
