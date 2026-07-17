package com.riprod.hexcode.core.common.node;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.node.component.NodeComponent;
import com.riprod.hexcode.utils.VfxUtil;

public class NodeRouter {

    public static InteractionState enter(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> nodeRef, Ref<EntityStore> playerRef) {
        NodeInterface handler = resolve(accessor, nodeRef);
        if (handler == null)
            return InteractionState.Failed;
        InteractionState result = handler.enter(accessor, nodeRef, playerRef);
        if (result == InteractionState.Finished) {
            playSound(NodeSounds.DRAG, accessor, nodeRef);
        }
        return result;
    }

    public static InteractionState click(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> nodeRef, Ref<EntityStore> playerRef) {
        NodeInterface handler = resolve(accessor, nodeRef);
        if (handler == null)
            return InteractionState.Failed;
        InteractionState result = handler.click(accessor, nodeRef, playerRef);
        if (result == InteractionState.Finished) {
            playSound(NodeSounds.CLICK, accessor, nodeRef);
        }
        return result;
    }

    public static InteractionState drag(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        NodeInterface handler = resolve(accessor, nodeRef);
        if (handler == null)
            return InteractionState.Failed;
        return handler.tick(accessor, nodeRef, playerRef);
    }

    public static InteractionState exit(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        NodeInterface handler = resolve(accessor, nodeRef);
        if (handler == null)
            return InteractionState.Failed;

        InteractionState result = handler.exit(accessor, nodeRef, playerRef);
        if (result == InteractionState.Finished) {
            playSound(NodeSounds.DROP, accessor, nodeRef);
        }
        return result;
    }

    public static InteractionState ability(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            InteractionType inputType, Ref<EntityStore> playerRef) {
        NodeInterface handler = resolve(accessor, nodeRef);
        if (handler == null)
            return InteractionState.Failed;

        InteractionState result = handler.ability(accessor, nodeRef, inputType, playerRef);
        if (result == InteractionState.Finished) {
            playSound(NodeSounds.DELETE, accessor, nodeRef);
        }
        return result;
    }

    public static void hover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        NodeInterface handler = resolve(accessor, nodeRef);
        if (handler == null)
            return;
        handler.hover(accessor, nodeRef, playerRef);
        playSound(NodeSounds.HOVER, accessor, nodeRef);
    }

    public static void unhover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef,
            Ref<EntityStore> playerRef) {
        NodeInterface handler = resolve(accessor, nodeRef);
        if (handler == null)
            return;
        handler.unhover(accessor, nodeRef, playerRef);
    }

    private static NodeInterface resolve(CommandBuffer<EntityStore> accessor, Ref<EntityStore> nodeRef) {
        NodeComponent nodeComp = accessor.getComponent(nodeRef, NodeComponent.getComponentType());
        if (nodeComp == null)
            return null;
        String configId = nodeComp.getConfigId();
        if (configId == null)
            return null;
        NodeConfig config = NodeConfig.getAssetMap().getAsset(configId);
        return config != null ? config.handler() : null;
    }

    private static void playSound(String soundId, CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> nodeRef) {
        TransformComponent transform = accessor.getComponent(nodeRef, TransformComponent.getComponentType());
        if (transform != null) {
            VfxUtil.sound(soundId, transform.getPosition(), accessor);
        }
    }
}
