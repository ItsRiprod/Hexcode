package com.riprod.hexcode.core.common.node;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;

public abstract class BaseNodeHandler implements NodeInterface {

    @Override
    public InteractionState enter(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        return InteractionState.Finished;
    }

    @Override
    public InteractionState tick(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        return InteractionState.Finished;
    }

    @Override
    public InteractionState exit(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        return InteractionState.Finished;
    }

    @Override
    public InteractionState click(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            Ref<EntityStore> playerRef) {
        return InteractionState.Finished;
    }

    @Override
    public InteractionState ability(CommandBuffer<EntityStore> accessor, Ref<EntityStore> node,
            InteractionType inputType, Ref<EntityStore> playerRef) {
        return InteractionState.Failed;
    }

    @Override
    public void despawn(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        accessor.tryRemoveEntity(nodeRef, RemoveReason.REMOVE);
    }

    @Override
    public void hover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        applyDebugHoverHighlight(accessor, nodeRef);
    }

    @Override
    public void unhover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        applyDebugHoverReset(accessor, nodeRef);
    }

    protected static void applyDebugHoverHighlight(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> nodeRef) {
        DebugComponent debug = accessor.getComponent(nodeRef, DebugComponent.getComponentType());
        if (debug == null) return;
        debug.setScaleMultiplier(1.3f);
        debug.setIntervalMultiplier(0.25f);
        debug.setFadeMultiplier(0.25f);
        debug.setTimer(0);
    }

    protected static void applyDebugHoverReset(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> nodeRef) {
        DebugComponent debug = accessor.getComponent(nodeRef, DebugComponent.getComponentType());
        if (debug == null) return;
        debug.resetScaleMultiplier();
        debug.resetFadeMultipler();
        debug.resetIntervalMultiplier();
    }
}
