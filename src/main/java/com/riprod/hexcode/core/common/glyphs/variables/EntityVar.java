package com.riprod.hexcode.core.common.glyphs.variables;

import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.redirect.EntityRedirectComponent;

public final class EntityVar extends HexVar {
    private PersistentRef entity;

    public EntityVar() {
    }

    public EntityVar(PersistentRef entity) {
        this.entity = entity;
    }

    public EntityVar(UUID entityId, Ref<EntityStore> ref) {
        this.entity = createRef(entityId, ref);
    }

    public static PersistentRef createRef(UUID entityId, Ref<EntityStore> ref) {
        PersistentRef persistent = new PersistentRef();
        persistent.setEntity(ref, entityId);
        return persistent;
    }

    @Override
    public HexVar copy() {
        return new EntityVar(entity);
    }

    @Nullable
    public Ref<EntityStore> getRef(ComponentAccessor<EntityStore> accessor) {
        Ref<EntityStore> ref = getRawRef(accessor);
        if (ref == null) return null;
        EntityRedirectComponent redirect = accessor.getComponent(ref, EntityRedirectComponent.getComponentType());
        if (redirect == null) return ref;
        Ref<EntityStore> deferral = redirect.resolveDeferral(accessor);
        return deferral != null ? deferral : ref;
    }

    @Nullable
    public Ref<EntityStore> getRawRef(ComponentAccessor<EntityStore> accessor) {
        if (entity == null) return null;
        return entity.getEntity(accessor);
    }

    @Nullable
    public PersistentRef getPersistentRef() {
        return entity;
    }

    @Override
    public Double toScalar() {
        return entity != null ? 1.0 : 0.0;
    }

    @Override
    public PositionVar toPosition(ComponentAccessor<EntityStore> accessor) {
        Ref<EntityStore> entityRef = getRef(accessor);
        if (entityRef == null || !entityRef.isValid()) {
            return new PositionVar(new Vector3d(0, 0, 0), true);
        }
        Vector3d pos = new Vector3d(accessor.getComponent(entityRef, TransformComponent.getComponentType()).getPosition());
        return new PositionVar(pos, true);
    }

    @Override
    public RotationVar toRotation(ComponentAccessor<EntityStore> accessor) {
        Ref<EntityStore> entityRef = getRef(accessor);
        if (entityRef == null || !entityRef.isValid()) {
            return new RotationVar(new Rotation3f());
        }
        try {
            HeadRotation headRot = accessor.getComponent(entityRef, HeadRotation.getComponentType());
            if (headRot != null) return new RotationVar(headRot.getRotation());
        } catch (Exception e) {
        }
        Rotation3f r = accessor.getComponent(entityRef, TransformComponent.getComponentType()).getRotation();
        return new RotationVar(r);
    }

    @Override
    public HexVar resolveSelf(HexVar partner, ComponentAccessor<EntityStore> accessor) {
        return partner instanceof RotationVar ? toRotation(accessor) : toPosition(accessor);
    }

    @Override
    public String describe() {
        if (entity == null) return "EntityVar: [null]";
        UUID id = entity.getUuid();
        if (id == null) return "EntityVar: [unset]";
        String s = id.toString();
        return "EntityVar: " + s.substring(0, Math.min(8, s.length()));
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof EntityVar ev) {
            if (entity == null || ev.entity == null) return entity == ev.entity;
            return entity.getUuid().equals(ev.entity.getUuid());
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof EntityVar ev) {
            if (entity == null && ev.entity == null) return 0;
            if (entity == null) return -1;
            if (ev.entity == null) return 1;
            return entity.getUuid().compareTo(ev.entity.getUuid());
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return "EntityVar(" + entity.getUuid() + ")";
    }

    public static final BuilderCodec<EntityVar> CODEC = BuilderCodec
            .builder(EntityVar.class, EntityVar::new, HexVar.BASE_CODEC)
            .append(new KeyedCodec<>("Entity", PersistentRef.CODEC),
                    (v, ref) -> v.entity = ref,
                    v -> v.entity)
            .add()
            .build();

    static {
        HexVar.CODEC.register("Entity", EntityVar.class, EntityVar.CODEC);
    }
}
