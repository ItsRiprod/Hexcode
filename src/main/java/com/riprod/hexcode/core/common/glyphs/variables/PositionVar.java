package com.riprod.hexcode.core.common.glyphs.variables;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;

import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.utils.BlockResolution;

public final class PositionVar extends HexVar {
    private Vector3d position;

    public PositionVar() {
    }

    public PositionVar(Vector3d position) {
        this.position = position;
    }

    public Vector3d getValue() {
        return position;
    }
    
    @Override
    public VarType type() {
        return VarType.POSITION;
    }

    @Override
    public HexVar copy() {
        return new PositionVar(position == null ? null : new Vector3d(position));
    }

    @Override
    public Double scalar() {
        if (position == null)
            return 0.0;
        double x = position.x, y = position.y, z = position.z;
        boolean nx = x != 0.0, ny = y != 0.0, nz = z != 0.0;
        int count = (nx ? 1 : 0) + (ny ? 1 : 0) + (nz ? 1 : 0);
        if (count == 1)
            return nx ? x : ny ? y : z;
        return position.length();
    }

    @Override
    public Vector3d position(ComponentAccessor<EntityStore> accessor) {
        return new Vector3d(position);
    }

    @Override
    public Rotation3f rotation(ComponentAccessor<EntityStore> accessor) {
        if (position == null) return new Rotation3f();
        return Rotation3f.lookAt(position);
    }

    @Override
    public String describe() {
        if (position == null)
            return "PositionVar: [null]";
        return String.format("PositionVar: (%.2f, %.2f, %.2f)",
                position.x, position.y, position.z);
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof PositionVar pb) {
            if (position == null || pb.position == null)
                return position == pb.position;
            return position.equals(pb.position);
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof PositionVar pb) {
            if (position == null && pb.position == null)
                return 0;
            if (position == null)
                return -1;
            if (pb.position == null)
                return 1;
            return Double.compare(position.length(), pb.position.length());
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return "PositionVar(" + position + ")";
    }

    public static final BuilderCodec<PositionVar> CODEC = BuilderCodec
            .builder(PositionVar.class, PositionVar::new, HexVar.BASE_CODEC)
            .append(new KeyedCodec<>("Position", Vector3dUtil.CODEC),
                    (v, pos) -> v.position = pos,
                    v -> v.position)
            .add()
            .build();

    static {
        HexVar.CODEC.register("Position", PositionVar.class, PositionVar.CODEC);
    }
}
