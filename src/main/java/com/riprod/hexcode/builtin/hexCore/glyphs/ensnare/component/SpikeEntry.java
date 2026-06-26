package com.riprod.hexcode.builtin.hexCore.glyphs.ensnare.component;

import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SpikeEntry {

    private final Vector3d position;
    private final Ref<EntityStore> entityRef;

    public SpikeEntry(Vector3d position, Ref<EntityStore> entityRef) {
        this.position = position;
        this.entityRef = entityRef;
    }

    public Vector3d getPosition() {
        return position;
    }

    public Ref<EntityStore> getEntityRef() {
        return entityRef;
    }
}
