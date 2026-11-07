package com.riprod.hexcode.builtin.elementalism.element.efficiency;

import org.joml.Vector3dc;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class BranchEfficiency {
    public static final CodecMapCodec<BranchEfficiency> CODEC = new CodecMapCodec<>("Type");

    public abstract float efficiency(float elapsedActive, int branches, Vector3dc velocity,
            Ref<EntityStore> host, ComponentAccessor<EntityStore> accessor);
}
