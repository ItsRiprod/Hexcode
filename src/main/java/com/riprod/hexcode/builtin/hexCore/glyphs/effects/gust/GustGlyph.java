package com.riprod.hexcode.builtin.hexCore.glyphs.effects.gust;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.PointKnockback;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class GustGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Gust";

    // purposefully a random number to avoid intentional offsets causing the divide-by-zero bug (obtained by keyboard spam)
    private double minKnockbackOffset = 0.15792347;
    private float entityDamage = 0.0f;
    private float entityDamageFalloff = 1.0f;
    private double verticalKnockbackScale = 0.3;

    public static final BuilderCodec<GustGlyph> CODEC = BuilderCodec
            .builder(GustGlyph.class, GustGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("MinKnockbackOffset", Codec.DOUBLE, true),
                    (c, v) -> c.minKnockbackOffset = v, c -> c.minKnockbackOffset)
            .add()
            .append(new KeyedCodec<>("EntityDamage", Codec.FLOAT, true),
                    (c, v) -> c.entityDamage = v, c -> c.entityDamage)
            .add()
            .append(new KeyedCodec<>("EntityDamageFalloff", Codec.FLOAT, true),
                    (c, v) -> c.entityDamageFalloff = v, c -> c.entityDamageFalloff)
            .add()
            .append(new KeyedCodec<>("VerticalKnockbackScale", Codec.DOUBLE, true),
                    (c, v) -> c.verticalKnockbackScale = v, c -> c.verticalKnockbackScale)
            .add()
            .build();

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        double radius = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GustGlyphSlots.RADIUS, hexContext),
                asset != null ? asset.getSlot(GustGlyphSlots.RADIUS) : null));
        double mag = Math.abs(HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GustGlyphSlots.MAGNITUDE, hexContext),
                asset != null ? asset.getSlot(GustGlyphSlots.MAGNITUDE) : null));
        double effective = mag * radius * radius;

        Impact impact = asset == null
                ? null
                : asset.getVolatilityImpact();
        return glyph.computeBaseCost(asset) * Impact.scale(impact, effective);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar centerVar = glyph.readSlot(GustGlyphSlots.CENTER, hexContext);

        double radius = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GustGlyphSlots.RADIUS, hexContext), getSlot(GustGlyphSlots.RADIUS));
        double mag = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GustGlyphSlots.MAGNITUDE, hexContext), getSlot(GustGlyphSlots.MAGNITUDE));

        if (centerVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Center required");
            return;
        }

        Vector3d center = centerVar.position(hexContext.getAccessor());
        if (center == null) {
            center = hexContext.getDefaultVariable().position(hexContext.getAccessor());
        }
        if (center == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Center ref unresolved");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        Vector3d explosionCenter = new Vector3d(center).add(0, minKnockbackOffset, 0);

        ExplosionConfig explosionConfig = new ExplosionConfig() {
            {
                damageEntities = true;
                damageBlocks = false;
                entityDamageRadius = (float) radius;
                entityDamage = GustGlyph.this.entityDamage;
                entityDamageFalloff = GustGlyph.this.entityDamageFalloff;
                knockback = new PointKnockback() {
                    {
                        force = (float) mag;
                        velocityY = (float) (mag * verticalKnockbackScale);
                        duration = 0;
                    }
                };
            }
        };

        ExplosionUtils.performExplosion(
                new Damage.EnvironmentSource("hex_gust"),
                explosionCenter,
                null,
                explosionConfig,
                null,
                accessor,
                accessor.getExternalData().getWorld().getChunkStore().getStore());

        EffectRenderer.render(EffectSlot.Start, EffectRenderer.resolve(this, hexContext), center, null, (float) radius,
                null, accessor);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
