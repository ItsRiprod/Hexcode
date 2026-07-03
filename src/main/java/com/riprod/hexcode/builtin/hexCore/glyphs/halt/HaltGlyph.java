package com.riprod.hexcode.builtin.hexCore.glyphs.halt;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.halt.style.HaltStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class HaltGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Halt";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(HaltConfig.class, HaltConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(HaltGlyphSlots.TARGET, hexContext);
        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target ref unresolved");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        HaltConfig config = getConfig(HaltConfig.class, asset);
        if (config == null) config = HaltConfig.DEFAULTS;

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(HaltGlyphSlots.DURATION, hexContext),
                asset.getSlot(HaltGlyphSlots.DURATION));

        try {
            StandardPhysicsProvider physics = accessor.getComponent(ref,
                    StandardPhysicsProvider.getComponentType());
            if (physics != null) {
                physics.getForceProviderStandardState().nextTickVelocity.set(0d, 0d, 0d);
                if (duration > 0) {
                    physics.setState(StandardPhysicsProvider.STATE.INACTIVE);
                }
            } else {
                Velocity vel = accessor.getComponent(ref, Velocity.getComponentType());
                if (vel != null) {
                    vel.getInstructions().clear();
                    vel.addInstruction(new Vector3d(), null, ChangeVelocityType.Set);
                }
            }

            if (duration > 0) {
                HexConstructSpawner.applyWithState(accessor, ref, hexContext, glyph, HaltGlyph.ID,
                        new HaltState((float) duration, config.getEffectId(), glyph.getNextLinks()));
            }

            if (duration > 0) {
                EntityEffect haltEffect = EntityEffect.getAssetMap().getAsset(config.getEffectId());
                if (haltEffect != null) {
                    EffectControllerComponent controller = accessor.getComponent(
                            ref, EffectControllerComponent.getComponentType());
                    if (controller != null) {
                        controller.addEffect(ref, haltEffect, (float) duration,
                                OverlapBehavior.OVERWRITE, accessor);
                    }
                } else {
                    LOGGER.atWarning().log("halt: %s effect asset not found", config.getEffectId());
                }
            }

            TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
            if (tc != null) {
                HaltStyle.render(tc.getPosition(), hexContext, accessor);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("halt: could not halt entity: %s", e.getMessage());
        }

        if (duration <= 0) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }
}
