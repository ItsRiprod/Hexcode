package com.riprod.hexcode.core.common.pedestal.events;

import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;

public class PedestalInteractEvent implements IEvent<Void> {

    private final CommandBuffer<EntityStore> buffer;
    private final Ref<EntityStore> playerRef;
    private final PedestalBlockComponent pedestal;
    private final Vector3i blockPos;

    public PedestalInteractEvent(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            PedestalBlockComponent pedestal, Vector3i blockPos) {
        this.buffer = buffer;
        this.playerRef = playerRef;
        this.pedestal = pedestal;
        this.blockPos = blockPos;
    }

    public CommandBuffer<EntityStore> getBuffer() {
        return buffer;
    }

    public Ref<EntityStore> getPlayerRef() {
        return playerRef;
    }

    public PedestalBlockComponent getPedestal() {
        return pedestal;
    }

    public Vector3i getBlockPos() {
        return blockPos;
    }
}
