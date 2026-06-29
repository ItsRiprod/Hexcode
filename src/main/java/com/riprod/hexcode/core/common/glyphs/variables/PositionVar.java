package com.riprod.hexcode.core.common.glyphs.variables;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Vector3dUtil;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.utils.BlockResolution;

public final class PositionVar extends HexVar {
    private Vector3d position;
    private boolean absolute;

    public PositionVar() {
    }

    public PositionVar(Vector3d position) {
        this.position = position;
    }

    public PositionVar(Vector3d position, boolean absolute) {
        this.position = position;
        this.absolute = absolute;
    }

    public boolean isAbsolute() {
        return absolute;
    }

    public Vector3d getValue() {
        return position;
    }

    @Override
    public HexVar copy() {
        return new PositionVar(position == null ? null : new Vector3d(position), absolute);
    }

    @Override
    public Object getRawValue() {
        return position;
    }

    @Override
    public Double toScalar() {
        if (position == null) return 0.0;
        double x = position.x, y = position.y, z = position.z;
        boolean nx = x != 0.0, ny = y != 0.0, nz = z != 0.0;
        int count = (nx ? 1 : 0) + (ny ? 1 : 0) + (nz ? 1 : 0);
        if (count == 1) return nx ? x : ny ? y : z;
        return position.length();
    }

    @Override
    public PositionVar toPosition(ComponentAccessor<EntityStore> accessor) {
        return this;
    }

    @Override
    public BlockVar toBlockVar(ComponentAccessor<EntityStore> accessor) {
        if (position == null) return new BlockVar(null);
        World world = accessor != null ? accessor.getExternalData().getWorld() : null;
        Vector3i resolved = BlockResolution.resolveSolidBlock(world, position);
        return new BlockVar(resolved);
    }

    @Override
    public RotationVar toRotation(ComponentAccessor<EntityStore> accessor) {
        if (position == null) return new RotationVar(new Vector3f(0f, 0f, 0f));
        double len = position.length();
        if (len == 0) return new RotationVar(new Vector3f(0f, 0f, 0f));
        double yaw = Math.atan2(-position.x, position.z);
        double pitch = Math.asin(-position.y / len);
        return new RotationVar(new Vector3f((float) pitch, (float) yaw, 0f));
    }

    @Override
    public String describe() {
        if (position == null) return "PositionVar: [null]";
        return String.format("PositionVar: (%.2f, %.2f, %.2f) [%s]",
                position.x, position.y, position.z, absolute ? "abs" : "rel");
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof PositionVar pb) {
            if (position == null || pb.position == null) return position == pb.position;
            return position.equals(pb.position);
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof PositionVar pb) {
            if (position == null && pb.position == null) return 0;
            if (position == null) return -1;
            if (pb.position == null) return 1;
            return Double.compare(position.length(), pb.position.length());
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return "PositionVar(" + position + ", absolute=" + absolute + ")";
    }

    public static final BuilderCodec<PositionVar> CODEC = BuilderCodec
            .builder(PositionVar.class, PositionVar::new, HexVar.BASE_CODEC)
            .append(new KeyedCodec<>("Position", Vector3dUtil.CODEC),
                    (v, pos) -> v.position = pos,
                    v -> v.position)
            .add()
            .append(new KeyedCodec<>("Absolute", Codec.BOOLEAN),
                    (v, abs) -> v.absolute = abs,
                    v -> v.absolute)
            .add()
            .build();

    static {
        HexVar.CODEC.register("Position", PositionVar.class, PositionVar.CODEC);
    }
}
