package com.riprod.hexcode.core.common.effect;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphStyleUtil;

public class GlyphEffectSystem extends RefSystem<EntityStore> {
    private static HytaleLogger logger = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return GlyphComponent.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {

        try {
            GlyphComponent glyph = store.getComponent(ref, GlyphComponent.getComponentType());
            if (glyph == null) {
                logger.atFine().log("GlyphEffectSystem: Entity %s has no GlyphComponent", ref);
                return;
            }

            EntityEffect effect = GlyphStyleUtil.getGlyphEffect(glyph.getVolatility(), glyph.getEfficiency());
            if (effect == null) {
                logger.atWarning().log("GlyphEffectSystem: No effect found for volatility %s and efficiency %s",
                        glyph.getVolatility(), glyph.getEfficiency());
                return;
            }

            EffectControllerComponent effectController = store.getComponent(ref,
                    EffectControllerComponent.getComponentType());
            if (effectController == null) {
                logger.atFine().log("GlyphEffectSystem: Entity %s - %s has no EffectControllerComponent", glyph, effect.getId());
                return;
            }

            effectController.addEffect(ref, effect, buffer);
        } catch (Exception e) {
            logger.atSevere().log("[hexcode] GlyphEffectSystem.onEntityAdded failed: %s", e.getMessage());
        }
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
    }
}
