package com.riprod.hexcode.builtin.ums.api;

import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class UmsReactionEvent extends CancellableEcsEvent {

    private final Ref<EntityStore> targetRef;
    private final String elementId;
    private final String causeId;
    private final Damage damage;
    @Nullable
    private final Vector3d hitPosition;

    public UmsReactionEvent(Ref<EntityStore> targetRef, String elementId, String causeId,
            Damage damage, @Nullable Vector3d hitPosition) {
        this.targetRef = targetRef;
        this.elementId = elementId;
        this.causeId = causeId;
        this.damage = damage;
        this.hitPosition = hitPosition;
    }

    public Ref<EntityStore> getTargetRef() {
        return targetRef;
    }

    public String getElementId() {
        return elementId;
    }

    public String getCauseId() {
        return causeId;
    }

    public Damage getDamage() {
        return damage;
    }

    @Nullable
    public Vector3d getHitPosition() {
        return hitPosition;
    }
}
