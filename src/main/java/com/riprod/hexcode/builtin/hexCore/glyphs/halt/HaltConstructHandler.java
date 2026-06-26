package com.riprod.hexcode.builtin.hexCore.glyphs.halt;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;

public class HaltConstructHandler implements ConstructHandler<HaltState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String HALT_EFFECT_ID = "Hexcode_Halt";

    @Override
    public boolean onTick(float dt, HexStatus<HaltState> status, ConstructTickContext ctx) {
        HaltState state = status.getState();
        if (state == null) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        clampVelocity(ctx);
        return !drainSustain(dt, status);
    }

    private void clampVelocity(ConstructTickContext ctx) {
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid()) return;
        Velocity vel = ctx.getBuffer().getComponent(target, Velocity.getComponentType());
        if (vel == null) return;
        vel.getInstructions().clear();
        vel.addInstruction(new Vector3d(), null, ChangeVelocityType.Set);
    }

    @Override
    public void onEnd(HexStatus<HaltState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        HaltState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("halt: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<HaltState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("halt: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<HaltState> status) {
        HaltState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<HaltState> status, List<String> ids) {
        HaltState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<HaltState> status, ConstructTickContext ctx) {
        HaltState state = status.getState();
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid()) return;

        StandardPhysicsProvider physics = buffer.getComponent(
                target, StandardPhysicsProvider.getComponentType());
        if (physics != null) {
            physics.setState(StandardPhysicsProvider.STATE.ACTIVE);
        }

        if (state != null && state.getOriginalPhysicsValues() != null) {
            buffer.putComponent(target, PhysicsValues.getComponentType(),
                    new PhysicsValues(state.getOriginalPhysicsValues()));
        }

        EffectControllerComponent controller = buffer.getComponent(
                target, EffectControllerComponent.getComponentType());
        if (controller != null) {
            int effectIndex = EntityEffect.getAssetMap().getIndex(HALT_EFFECT_ID);
            if (effectIndex != Integer.MIN_VALUE) {
                controller.removeEffect(target, effectIndex, buffer);
            }
        }
    }
}
