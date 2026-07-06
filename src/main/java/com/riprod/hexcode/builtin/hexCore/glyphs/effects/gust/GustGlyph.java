package com.riprod.hexcode.builtin.hexCore.glyphs.effects.gust;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.PointKnockback;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class GustGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Gust";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(GustConfig.class, GustConfig.CODEC);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        double radius = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GustGlyphSlots.RADIUS, hexContext),
                asset != null ? asset.getSlot(GustGlyphSlots.RADIUS) : null));
        double mag = Math.max(0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GustGlyphSlots.MAGNITUDE, hexContext),
                asset != null ? asset.getSlot(GustGlyphSlots.MAGNITUDE) : null));
        double effective = mag * radius * radius;

        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getVolatilityImpact();
        return glyph.computeBaseCost(asset) * Impact.scale(impact, effective);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar centerVar = glyph.readSlot(GustGlyphSlots.CENTER, hexContext);

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        GustConfig config = getConfig(GustConfig.class, asset);
        if (config == null) config = GustConfig.DEFAULTS;

        double radius = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GustGlyphSlots.RADIUS, hexContext), asset.getSlot(GustGlyphSlots.RADIUS));
        double mag = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(GustGlyphSlots.MAGNITUDE, hexContext), asset.getSlot(GustGlyphSlots.MAGNITUDE));

        if (centerVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Center required");
            return;
        }

        Vector3d center = HexVarUtil.position(centerVar, hexContext.getAccessor());
        if (center == null) {
            center = HexVarUtil.position(
                    hexContext.getDefaultVariable(), hexContext.getAccessor());
        }
        if (center == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Center ref unresolved");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        Vector3d explosionCenter = new Vector3d(center).add(0, config.getMinKnockbackOffset(), 0);

        final GustConfig gustConfig = config;
        ExplosionConfig explosionConfig = new ExplosionConfig() {
            {
                damageEntities = true;
                damageBlocks = false;
                entityDamageRadius = (float) radius;
                entityDamage = gustConfig.getEntityDamage();
                entityDamageFalloff = gustConfig.getEntityDamageFalloff();
                knockback = new PointKnockback() {
                    {
                        force = (float) mag;
                        velocityY = (float) (mag * gustConfig.getVerticalKnockbackScale());
                        duration = 0;
                    }
                };
            }
        };

        ExplosionUtils.performExplosion(
                new Damage.EnvironmentSource("hex_gust"),
                explosionCenter,
                explosionConfig,
                null,
                accessor,
                accessor.getExternalData().getWorld().getChunkStore().getStore());

        GustGlyphStyle.render(center, radius, hexContext, accessor);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
