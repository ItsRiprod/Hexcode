package com.riprod.hexcode.builtin.hexCore.glyphs.levitate;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.levitate.style.LevitateStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class LevitateGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Levitate";

    private static final String LEVITATE_EFFECT_ID = "Hexcode_Levitate";
    private static final double DEFAULT_INTENSITY = 1.0;
    private static final double DEFAULT_DURATION = 10.0;

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(LevitateGlyphSlots.TARGET, hexContext);
        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is no longer available");
            return;
        }

        float intensity = (float) Math.max(0,
                HexVarUtil.numberOrDefault(
                        glyph.readSlot(LevitateGlyphSlots.INTENSITY, hexContext), DEFAULT_INTENSITY));
        float durationSeconds = (float) Math.max(1,
                HexVarUtil.numberOrDefault(
                        glyph.readSlot(LevitateGlyphSlots.DURATION, hexContext), DEFAULT_DURATION));

        try {
            LevitateState state = new LevitateState();
            state.setAppliedIntensity(intensity);
            state.setRemainingDuration(durationSeconds);
            state.setColors(hexContext.getColors());
            state.setNextGlyphIds(glyph.getNextLinks());

            LevitateStackComponent stack = accessor.getComponent(
                    ref, LevitateStackComponent.getComponentType());
            if (stack == null) {
                stack = new LevitateStackComponent();
            }
            stack.put(state.getConstructId(), intensity);
            accessor.putComponent(ref, LevitateStackComponent.getComponentType(), stack);

            applyLevitation(ref, accessor);

            EntityEffect levitateEffect = EntityEffect.getAssetMap().getAsset(LEVITATE_EFFECT_ID);
            if (levitateEffect != null) {
                EffectControllerComponent controller = accessor.getComponent(
                        ref, EffectControllerComponent.getComponentType());
                if (controller != null) {
                    controller.addEffect(ref, levitateEffect, durationSeconds,
                            OverlapBehavior.OVERWRITE, accessor);
                }
            } else {
                LOGGER.atWarning().log("levitate: %s effect asset not found", LEVITATE_EFFECT_ID);
            }

            HexConstructSpawner.applyWithState(
                    accessor, ref, hexContext, glyph, LevitateGlyph.ID, state);

            TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
            if (tc != null) {
                LevitateStyle.renderActivation(tc.getPosition(), hexContext, accessor);
            }
        } catch (Exception e) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot apply levitate", e);
        }
    }

    public static void applyLevitation(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer) {
        MovementManager mm = buffer.getComponent(ref, MovementManager.getComponentType());
        if (mm != null) {
            mm.getSettings().invertedGravity = true;
            PlayerRef playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef != null) {
                mm.update(playerRef.getPacketHandler());
            }
            return;
        }
        PhysicsValues current = buffer.getComponent(ref, PhysicsValues.getComponentType());
        if (current != null) {
            buffer.putComponent(ref, PhysicsValues.getComponentType(),
                    new PhysicsValues(current.getMass(), current.getDragCoefficient(), true));
        }
    }

    public static void clearLevitation(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer) {
        MovementManager mm = buffer.getComponent(ref, MovementManager.getComponentType());
        if (mm != null) {
            mm.getSettings().invertedGravity = false;
            PlayerRef playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef != null) {
                mm.update(playerRef.getPacketHandler());
            }
            return;
        }
        PhysicsValues current = buffer.getComponent(ref, PhysicsValues.getComponentType());
        if (current != null) {
            buffer.putComponent(ref, PhysicsValues.getComponentType(),
                    new PhysicsValues(current.getMass(), current.getDragCoefficient(), false));
        }
    }
}
