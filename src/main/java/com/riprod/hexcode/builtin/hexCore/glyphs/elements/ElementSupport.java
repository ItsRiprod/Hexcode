package com.riprod.hexcode.builtin.hexCore.glyphs.elements;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public final class ElementSupport {

    private ElementSupport() {
    }

    @Nullable
    public static Ref<EntityStore> resolveTarget(Glyph glyph, HexContext hexContext) {
        HexVar subject = hexContext.getDefaultVariable();
        EntityVar entityVar = subject != null ? HexVarUtil.resolveEntityVar(subject, hexContext) : null;
        if (entityVar == null) return null;
        Ref<EntityStore> ref = entityVar.getRef(hexContext.getAccessor());
        return ref != null && ref.isValid() ? ref : null;
    }

    public static boolean applyStatus(Ref<EntityStore> target, CommandBuffer<EntityStore> accessor,
            String effectId, float seconds) {
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectId);
        if (effect == null) return false;
        EffectControllerComponent controller = accessor.getComponent(
                target, EffectControllerComponent.getComponentType());
        if (controller == null) return false;
        controller.addEffect(target, effect, seconds, OverlapBehavior.OVERWRITE, accessor);
        return true;
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
        return 1.0f + (value.get() / 100f) * scale;
    }
}
