package com.riprod.hexcode.builtin.hexCore.glyphs.effects.warp.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.warp.WarpGlyph;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class WarpStyle {

    private WarpStyle() {
    }

    public static void render(GlyphAsset asset, Vector3d departure, Vector3d arrival, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset, departure, accessor);
        VfxUtil.spawnSecondary(overrides, asset, arrival, accessor);
    }

    public static void applyEffect(WarpGlyph asset, HexVar var, HexContext ctx, ComponentAccessor<EntityStore> accessor) {
        if (!(var instanceof EntityVar entityVar)) {
            return;
        }
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) return;

        if (asset == null) return;
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(asset.getEffectId());
        if (effect == null) return;

        EffectControllerComponent controller = accessor.getComponent(
                ref, EffectControllerComponent.getComponentType());
        if (controller != null) controller.addEffect(ref, effect, accessor);
    }
}
