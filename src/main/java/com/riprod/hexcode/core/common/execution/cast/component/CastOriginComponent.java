package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import org.joml.Vector3d;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public final class CastOriginComponent implements CastComponent {

    private final Vector3d position;
    private final Rotation3f rotation;
    private final HexVar selfVariable;

    public CastOriginComponent(Vector3d position, Rotation3f rotation, HexVar selfVariable) {
        this.position = position;
        this.rotation = rotation;
        this.selfVariable = selfVariable;
    }

    public Vector3d getPosition() {
        return position;
    }

    public Rotation3f getRotation() {
        return rotation;
    }

    public HexVar getSelfVariable() {
        return selfVariable;
    }

    @Nonnull
    @Override
    public CastOriginComponent copy() {
        return new CastOriginComponent(new Vector3d(position), new Rotation3f(rotation),
                selfVariable != null ? selfVariable.copy() : null);
    }

    @Nonnull
    @Override
    public CastOriginComponent branch() {
        return this;
    }

    public static final CastComponentType<CastOriginComponent> TYPE = HexComponentRegistry.register(
            CastOriginComponent.class, "Origin", a -> {
                var accessor = a.accessor();
                var transform = accessor.getComponent(a.root(), TransformComponent.getComponentType());
                if (transform == null) return null;
                var uuid = accessor.getComponent(a.root(), UUIDComponent.getComponentType());
                if (uuid == null) return null;
                var head = accessor.getComponent(a.root(), HeadRotation.getComponentType());
                var rotation = head != null ? head.getRotation() : transform.getRotation();
                return new CastOriginComponent(new Vector3d(transform.getPosition()),
                        new Rotation3f(rotation), new EntityVar(uuid.getUuid(), a.root()));
            });
}
