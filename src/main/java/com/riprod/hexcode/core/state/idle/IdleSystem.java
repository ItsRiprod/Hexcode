package com.riprod.hexcode.core.state.idle;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexcasterIdleComponent;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.state.HexState;
import com.riprod.hexcode.state.HexcodeManager;
import com.riprod.hexcode.utils.CleanupUtils;

public class IdleSystem extends HexcodeManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String HOLD_STALE_KEY = "idle_hold_stale";
    private static final float HOLD_STALE_THRESHOLD = 0.15f;

    @Override
    public void firstTick(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
            HexState previousState) {

        HexcasterCraftingComponent craftingComp = buffer.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            Ref<EntityStore> headAnchor = craftingComp.getHeadAnchorRef();
            if (headAnchor != null && headAnchor.isValid()) {
                CleanupUtils.safeRemoveEntity(buffer, headAnchor);
            }
            craftingComp.clear(buffer);
        }

        buffer.ensureComponent(ref, HexcasterIdleComponent.getComponentType());
        comp.setTickLength(HOLD_STALE_KEY, 0f);
    }

    @Override
    public void lastTick(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer,
            HexState nextState) {
        HexcasterIdleComponent idleComp = buffer.getComponent(ref, HexcasterIdleComponent.getComponentType());
        if (idleComp == null) return;
        idleComp.setHoldingPrimary(false);
        comp.setTickLength(HOLD_STALE_KEY, 0f);
    }

    @Override
    public void tick0(Ref<EntityStore> ref, HexcasterComponent comp, float dt,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        HexcasterIdleComponent idleComp = buffer.ensureAndGetComponent(ref,
                HexcasterIdleComponent.getComponentType());
        if (idleComp.isHoldingPrimary()) {
            comp.incrementTickLength(HOLD_STALE_KEY, dt);
            if (comp.getTickLength(HOLD_STALE_KEY) > HOLD_STALE_THRESHOLD) {
                idleComp.setHoldingPrimary(false);
            }
        }
    }

    @Override
    public void onPlayerJoin(Ref<EntityStore> playerRef, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    }

    @Override
    public void onPlayerLeave(Ref<EntityStore> ref, HexcasterComponent comp,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
        HexcasterIdleComponent idleComp = buffer.getComponent(ref, HexcasterIdleComponent.getComponentType());
        if (idleComp == null) return;
        idleComp.cancelAll(ref);
    }

    @Override
    public InteractionState enterAbility(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterComponent comp, InteractionType inputType) {
        HexcasterIdleComponent idleComp = buffer.getComponent(ref, HexcasterIdleComponent.getComponentType());
        if (inputType == InteractionType.Ability1) {
            int count = idleComp != null ? idleComp.getActiveCount() : 0;
            if (idleComp != null) {
                idleComp.cancelAll(ref);
            }
            PlayerRef pr = buffer.getComponent(ref, PlayerRef.getComponentType());
            if (pr != null && count > 0) {
                pr.sendMessage(Message.raw("dispelled " + count + " active spell(s)"));
            }
            return InteractionState.Finished;
        }
        return InteractionState.Finished;
    }

    @Override
    public InteractionState enterInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> ref,
            HexcasterComponent comp) {
        HexcasterIdleComponent idleComp = accessor.getComponent(ref, HexcasterIdleComponent.getComponentType());
        if (idleComp == null) {
            LOGGER.atWarning().log("no idle component on hexcaster, cannot execute");
            return InteractionState.Finished;
        }

        idleComp.setHoldingPrimary(true);
        comp.setTickLength(HOLD_STALE_KEY, 0f);
        return InteractionState.Finished;
    }

    @Override
    public InteractionState tickInteraction(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref, float dt,
            HexcasterComponent comp) {
        HexcasterIdleComponent idleComp = buffer.ensureAndGetComponent(ref,
                HexcasterIdleComponent.getComponentType());
        if (idleComp == null) return InteractionState.Finished;

        idleComp.setHoldingPrimary(true);
        comp.setTickLength(HOLD_STALE_KEY, 0f);
        return InteractionState.NotFinished;
    }

    @Override
    public InteractionState exitInteraction(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref,
            HexcasterComponent comp) {
        HexcasterIdleComponent idleComp = buffer.ensureAndGetComponent(ref,
                HexcasterIdleComponent.getComponentType());
        if (idleComp == null) return InteractionState.Finished;

        idleComp.setHoldingPrimary(false);
        comp.setTickLength(HOLD_STALE_KEY, 0f);
        return InteractionState.Finished;
    }
}
