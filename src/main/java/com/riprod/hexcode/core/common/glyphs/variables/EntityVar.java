package com.riprod.hexcode.core.common.glyphs.variables;

import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.redirect.EntityRedirectComponent;
import com.riprod.hexcode.utils.HexRefs;

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
    public VarType type() {
        return VarType.TRANSFORM;
    }

    @Override
    public HexVar copy() {
        return new EntityVar(entity);
    }

    @Nullable
    public Ref<EntityStore> getRef(ComponentAccessor<EntityStore> accessor) {
        Ref<EntityStore> ref = getRawRef(accessor);
        if (ref == null)
            return null;
        EntityRedirectComponent redirect = accessor.getComponent(ref, EntityRedirectComponent.getComponentType());
        if (redirect == null)
            return ref;
        Ref<EntityStore> deferral = HexRefs.live(redirect.resolveDeferral(accessor), accessor);
        return deferral != null ? deferral : ref;
    }

    @Nullable
    public Ref<EntityStore> getRawRef(ComponentAccessor<EntityStore> accessor) {
        return HexRefs.resolve(entity, accessor);
    }

    @Nullable
    public PersistentRef getPersistentRef() {
        return entity;
    }

    @Override
    public Double scalar() {
        return entity != null ? 1.0 : 0.0;
    }

    @Override
    public Vector3d position(ComponentAccessor<EntityStore> accessor) {
        Ref<EntityStore> entityRef = getRef(accessor);
        if (entityRef == null || !entityRef.isValid()) {
            return new Vector3d(0, 0, 0);
        }
        Vector3d pos = new Vector3d(
                accessor.getComponent(entityRef, TransformComponent.getComponentType()).getPosition());
        return pos;
    }

    @Override
    public Rotation3f rotation(ComponentAccessor<EntityStore> accessor) {
        Ref<EntityStore> entityRef = getRef(accessor);
        if (entityRef == null || !entityRef.isValid()) {
            return new Rotation3f();
        }
        try {
            HeadRotation headRot = accessor.getComponent(entityRef, HeadRotation.getComponentType());
            if (headRot != null)
                return headRot.getRotation();
        } catch (Exception e) {
        }
        Rotation3f r = accessor.getComponent(entityRef, TransformComponent.getComponentType()).getRotation();
        return r;
    }

    @Override
    public String describe() {
        if (entity == null)
            return "EntityVar: [null]";
        UUID id = entity.getUuid();
        if (id == null)
            return "EntityVar: [unset]";
        String s = id.toString();
        return "EntityVar: " + s.substring(0, Math.min(8, s.length()));
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof EntityVar ev) {
            if (entity == null || ev.entity == null)
                return entity == ev.entity;
            return entity.getUuid().equals(ev.entity.getUuid());
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof EntityVar ev) {
            if (entity == null && ev.entity == null)
                return 0;
            if (entity == null)
                return -1;
            if (ev.entity == null)
                return 1;
            return entity.getUuid().compareTo(ev.entity.getUuid());
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return "EntityVar(" + entity.getUuid() + ")";
    }

    @Override
    public Vector3d direction(ComponentAccessor<EntityStore> accessor) {
        Ref<EntityStore> entityRef = getRef(accessor);
        if (entityRef == null || !entityRef.isValid())
            return null;
        try {
            HeadRotation headRot = accessor.getComponent(entityRef, HeadRotation.getComponentType());
            if (headRot != null)
                return headRot.getDirection();
        } catch (Exception e) {
        }
        Rotation3f bodyRot = accessor.getComponent(entityRef, TransformComponent.getComponentType()).getRotation();
        return new RotationVar(bodyRot).forward();
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
