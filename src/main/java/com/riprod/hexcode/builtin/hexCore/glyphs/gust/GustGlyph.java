package com.riprod.hexcode.builtin.hexCore.glyphs.gust;

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
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class GustGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Gust";

    private static final double MIN_KNOCKBACK_OFFSET = 0.1;

    @Override
    public boolean consumeVolatility(Glyph glyph, HexContext hexContext) {
        HexStats tracker = hexContext.getVolatilityTracker();
        if (tracker == null) return true;

        double radius = Math.max(0, HexVarUtil.numberOrDefault(
                glyph.readSlot(GustGlyphSlots.RADIUS, hexContext), 5.0));
        double mag = Math.max(0, HexVarUtil.numberOrDefault(
                glyph.readSlot(GustGlyphSlots.MAGNITUDE, hexContext), 15.0));
        double effective = mag * radius * radius;

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getImpact();
        float cost = glyph.computeBaseCost() * Impact.scale(impact, effective);
        return tracker.consumeVolatility(cost) > 0f;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar centerVar = glyph.readSlot(GustGlyphSlots.CENTER, hexContext);
        double radius = HexVarUtil.numberOrDefault(glyph.readSlot(GustGlyphSlots.RADIUS, hexContext), 5.0);
        double mag = HexVarUtil.numberOrDefault(glyph.readSlot(GustGlyphSlots.MAGNITUDE, hexContext), 10.0);

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

        Vector3d explosionCenter = new Vector3d(center).add(0, MIN_KNOCKBACK_OFFSET, 0);

        ExplosionConfig config = new ExplosionConfig() {
            {
                damageEntities = true;
                damageBlocks = false;
                entityDamageRadius = (float) radius;
                entityDamage = 0.0f;
                entityDamageFalloff = 1.0f;
                knockback = new PointKnockback() {
                    {
                        force = (float) mag;
                        velocityY = (float) (mag * 0.3);
                        duration = 0;
                    }
                };
            }
        };

        ExplosionUtils.performExplosion(
                new Damage.EnvironmentSource("hex_gust"),
                explosionCenter,
                config,
                null,
                accessor,
                accessor.getExternalData().getWorld().getChunkStore().getStore());

        GustGlyphStyle.render(center, radius, hexContext, accessor);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
