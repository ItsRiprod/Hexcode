package com.riprod.hexcode.builtin.hexCore.glyphs.effects.swap.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.swap.SwapConfig;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class SwapStyle {

    private static final String GLYPH_ID = "Swap";

    private SwapStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void render(Vector3d posA, Vector3d posB, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset(), posA, accessor);
        VfxUtil.spawnSecondary(overrides, asset(), posB, accessor);
    }

    public static void applyEffect(HexVar var, HexContext ctx, ComponentAccessor<EntityStore> accessor) {
        EntityVar entityVar = HexVarUtil.resolveEntityVar(var, ctx);
        if (entityVar == null) return;
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) return;

        GlyphAsset glyphAsset = asset();
        GlyphConfig config = glyphAsset != null ? glyphAsset.getConfig() : null;
        String effectId = config instanceof SwapConfig swapConfig
                ? swapConfig.getEffectId() : SwapConfig.DEFAULTS.getEffectId();
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectId);
        if (effect == null) return;

        EffectControllerComponent controller = accessor.getComponent(
                ref, EffectControllerComponent.getComponentType());
        if (controller != null) controller.addEffect(ref, effect, accessor);
    }
}
