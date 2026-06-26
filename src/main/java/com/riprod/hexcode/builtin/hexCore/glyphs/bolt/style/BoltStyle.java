package com.riprod.hexcode.builtin.hexCore.glyphs.bolt.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class BoltStyle {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final String GLYPH_ID = "Bolt";
    private static final String SHOCK_EFFECT_ID = "Hexcode_Shock";
    private static final Vector3f DEFAULT_COLOR = new Vector3f(0.6f, 0.9f, 1.0f);
    private static final float BEAM_THICKNESS = 0.2f;
    private static final float BEAM_DURATION = 0.3f;
    private static final int PARTICLES_PER_BOLT = 12;

    private BoltStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static Vector3f resolveColor(HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        Color c = overrides != null ? overrides.getPrimaryColor() : null;
        if (c == null) {
            HexStyleAsset glyphStyle = asset() != null ? asset().getStyle() : null;
            c = glyphStyle != null ? glyphStyle.getPrimaryColor() : null;
        }
        return c != null ? HexColors.toVector3f(c) : DEFAULT_COLOR;
    }

    public static void renderBolt(ComponentAccessor<EntityStore> accessor, World world,
            Vector3d sourcePos, Vector3d targetPos, HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        Vector3f color = resolveColor(ctx);
        VfxUtil.line(accessor, world, sourcePos, targetPos, color, BEAM_THICKNESS, BEAM_DURATION, 0);
        String particleId = particleSystemId();
        if (particleId != null) {
            VfxUtil.particleAlongPath(particleId, sourcePos, targetPos, PARTICLES_PER_BOLT, accessor);
        }
        VfxUtil.spawnPrimary(overrides, asset(), sourcePos, accessor);
    }

    public static void renderImpact(ComponentAccessor<EntityStore> accessor, Vector3d position, HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), position, accessor);
    }

    public static void applyShockEffect(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> targetRef) {
        EntityEffect shockEffect = EntityEffect.getAssetMap().getAsset(SHOCK_EFFECT_ID);
        if (shockEffect == null) {
            LOGGER.atWarning().log("bolt: Hexcode_Shock effect asset not found");
            return;
        }

        EffectControllerComponent controller = accessor.getComponent(
                targetRef, EffectControllerComponent.getComponentType());
        if (controller == null) return;

        controller.addEffect(targetRef, shockEffect, 1.0f, OverlapBehavior.OVERWRITE, accessor);
    }

    private static String particleSystemId() {
        GlyphAsset a = asset();
        if (a == null || a.getStyle() == null || a.getStyle().getPrimaryParticle() == null) return null;
        return a.getStyle().getPrimaryParticle().getSystemId();
    }
}
