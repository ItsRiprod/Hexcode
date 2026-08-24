package com.riprod.hexcode.builtin.hexCore.glyphs.elements;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public final class ElementSupport {

    public static final String RESOURCE_LIMIT_SLOT = "ResourceLimit";
    public static final String TARGET_SLOT = "Target";
    public static final String ARCANE_RESOURCE = "Arcane";

    private ElementSupport() {
    }

    public static float resourceLimit(Glyph glyph, GlyphAsset asset, HexContext hexContext) {
        if (asset == null || asset.getSlot(RESOURCE_LIMIT_SLOT) == null) return -1f;
        return HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(RESOURCE_LIMIT_SLOT, hexContext),
                asset.getSlot(RESOURCE_LIMIT_SLOT)).floatValue();
    }

    public static float consumeResource(HexContext hexContext, Glyph glyph, @Nullable String resource,
            float cap) {
        String spender = glyph != null ? glyph.getGlyphId() : null;
        if (resource == null || resource.equals(ARCANE_RESOURCE)) {
            return hexContext.consumeResource(ARCANE_RESOURCE, spender, cap);
        }
        // arcane is the generic fallback, so the element pool drains first
        float fromElement = hexContext.consumeResource(resource, spender, cap);
        float remaining = cap < 0f ? -1f : Math.max(0f, cap - fromElement);
        if (cap >= 0f && remaining <= 1e-4f) return fromElement;
        return fromElement + hexContext.consumeResource(ARCANE_RESOURCE, spender, remaining);
    }

    @Nullable
    public static Ref<EntityStore> resolveTarget(Glyph glyph, HexContext hexContext) {
        HexVar subject = glyph.readSlot(TARGET_SLOT, hexContext);
        EntityVar entityVar = subject != null ? HexVarUtil.resolveEntityVar(subject, hexContext) : null;
        if (entityVar == null) return null;
        Ref<EntityStore> ref = entityVar.getRef(hexContext.getAccessor());
        return ref != null && ref.isValid() ? ref : null;
    }

    public static boolean applyStatus(HexContext hexContext, Ref<EntityStore> target, Glyph glyph,
            String effectId, float seconds) {
        return VfxUtil.applyBoundedEffect(hexContext, target, glyph, effectId, seconds,
                OverlapBehavior.OVERWRITE);
    }

    public static float scaledDuration(float complexity, float efficiency, float perComplexity,
            float affinity) {
        return complexity * efficiency * perComplexity * affinity;
    }

    public static float affinityFactor(HexContext hexContext, @Nullable String affinityStat, float scale) {
        if (affinityStat == null || affinityStat.isEmpty() || scale == 0f) return 1.0f;
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (caster == null || !caster.isValid()) return 1.0f;
        EntityStatMap stats = accessor.getComponent(caster, EntityStatMap.getComponentType());
        if (stats == null) return 1.0f;
        int index = EntityStatType.getAssetMap().getIndex(affinityStat);
        if (index == Integer.MIN_VALUE) return 1.0f;
        EntityStatValue value = stats.get(index);
        if (value == null) return 1.0f;
        return 1.0f + (value.getMax() / 100f) * scale;
    }
}
