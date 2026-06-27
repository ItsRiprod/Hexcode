package com.riprod.hexcode.core.common.glyphs.variables;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class RotationVar extends HexVar {
    private Rotation3f rotation;

    public RotationVar() {
    }

    public RotationVar(Rotation3f rotation) {
        this.rotation = rotation;
    }

    public RotationVar(Vector3f rotation) {
        this.rotation = rotation == null ? null : new Rotation3f(rotation.x, rotation.y, rotation.z);
    }

    public Rotation3f getValue() {
        return rotation;
    }

    @Override
    public Object getRawValue() {
        return rotation;
    }

    @Override
    public Double toScalar() {
        return rotation == null ? 0.0 : 1.0;
    }

    @Override
    public PositionVar toPosition(ComponentAccessor<EntityStore> accessor) {
        return new PositionVar(forward(), false);
    }

    @Override
    public RotationVar toRotation(ComponentAccessor<EntityStore> accessor) {
        return this;
    }

    public Vector3d forward() {
        if (rotation == null) return new Vector3d(0, 0, 1);
        double yaw = rotation.y;
        double pitch = rotation.x;
        double cosPitch = Math.cos(pitch);
        return new Vector3d(
                -Math.sin(yaw) * cosPitch,
                Math.sin(pitch),
                -Math.cos(yaw) * cosPitch);
    }

    @Override
    public String describe() {
        if (rotation == null)
            return "RotationVar: [null]";
        return String.format("RotationVar: ( p=%.3f, y=%.3f, r=%.3f )",
                rotation.x, rotation.y, rotation.z);
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof RotationVar rb) {
            if (rotation == null || rb.rotation == null)
                return rotation == rb.rotation;
            return rotation.equals(rb.rotation);
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof RotationVar rb) {
            if (rotation == null && rb.rotation == null)
                return 0;
            if (rotation == null)
                return -1;
            if (rb.rotation == null)
                return 1;
            return Double.compare(Math.sqrt(rotation.x*rotation.x + rotation.y*rotation.y + rotation.z*rotation.z),
                                  Math.sqrt(rb.rotation.x*rb.rotation.x + rb.rotation.y*rb.rotation.y + rb.rotation.z*rb.rotation.z));
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return "RotationVar(" + rotation + ")";
    }

    public static final BuilderCodec<RotationVar> CODEC = BuilderCodec
            .builder(RotationVar.class, RotationVar::new, HexVar.BASE_CODEC)
            .append(new KeyedCodec<>("Rotation", Rotation3f.CODEC),
                    (v, rot) -> v.rotation = rot,
                    v -> v.rotation)
            .add()
            .build();

    static {
        HexVar.CODEC.register("Rotation", RotationVar.class, RotationVar.CODEC);
    }
}
