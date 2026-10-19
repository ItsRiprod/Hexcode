package com.riprod.hexcode.core.common.glyphs.variables;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class HexVar {
    public static final CodecMapCodec<HexVar> CODEC = new CodecMapCodec<>("Type");
    public static final BuilderCodec<HexVar> BASE_CODEC = BuilderCodec.abstractBuilder(HexVar.class).build();

    public abstract String describe();

    public abstract HexVar copy();

    public abstract Double scalar();

    public Vector3d position(ComponentAccessor<EntityStore> accessor) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot convert to Position");
    }

    public Rotation3fc rotation(ComponentAccessor<EntityStore> accessor) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot convert to Rotation");
    }

    public Transform transform(ComponentAccessor<EntityStore> accessor) {
        return new Transform(position(accessor), rotation(accessor));
    }

    public Vector3d direction(ComponentAccessor<EntityStore> accessor) {
        var rotation = rotation(accessor);
        if (rotation == null) return new Vector3d(Vector3dUtil.FORWARD);
        return Vector3dUtil.setYawPitch(rotation.yaw(), rotation.pitch(), new Vector3d());
    }

    public boolean equalTo(HexVar other) {
        if (other == null)
            return false;
        return Double.compare(this.scalar(), other.scalar()) == 0;
    }

    public int compareTo(HexVar other) {
        if (other == null)
            return 1;
        return Double.compare(this.scalar(), other.scalar());
    }
}
