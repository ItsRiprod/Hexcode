package com.riprod.hexcode.core.common.glyphs.variables;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;

import javax.annotation.Nonnull;

import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class TransformVar extends HexVar {
    private Transform transform;

    public TransformVar() {
    }

    public TransformVar(Transform transform) {
        this.transform = transform;
    }
    public TransformVar(Vector3d pos, Rotation3f rot) {
        this.transform = new Transform(pos, rot);
    }

    public Transform getValue() {
        return transform;
    }
    
    @Override
    public VarType type() {
        return VarType.TRANSFORM;
    }

    @Override
    public HexVar copy() {
        return new TransformVar(transform);
    }

    @Override
    public Double scalar() {
        return transform.getPosition().length();
    }

    @Override
    @Nonnull
    public Vector3d position(ComponentAccessor<EntityStore> accessor) {
        return new Vector3d(transform.getPosition());
    }

    @Override
    @Nonnull
    public Rotation3f rotation(ComponentAccessor<EntityStore> accessor) {
        return new Rotation3f(transform.getRotation());
    }

    @Override 
    public Transform transform(ComponentAccessor<EntityStore> accessor) {
        return transform;
    }

    @Override
    public String describe() {
        return "TransformVar: " + transform;
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof TransformVar nb) {
            return this.transform.equals(nb.transform);
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof TransformVar tb) {
            if (transform == null && tb.transform == null)
                return 0;
            if (transform == null)
                return -1;
            if (tb.transform == null)
                return 1;
            return Double.compare(transform.getPosition().length(), tb.transform.getPosition().length());
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return String.valueOf(transform);
    }   

    public static final BuilderCodec<TransformVar> CODEC = BuilderCodec
            .builder(TransformVar.class, TransformVar::new, HexVar.BASE_CODEC)
            .append(new KeyedCodec<>("Value", Transform.CODEC),
                    (v, transform) -> v.transform = transform,
                    v -> v.transform)
            .add()
            .build();

    static {
        HexVar.CODEC.register("transform", TransformVar.class, TransformVar.CODEC);
    }
}
