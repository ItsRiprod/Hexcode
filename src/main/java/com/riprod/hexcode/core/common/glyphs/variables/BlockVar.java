package com.riprod.hexcode.core.common.glyphs.variables;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3iUtil;

import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class BlockVar extends HexVar {
    private Vector3i position;

    public BlockVar() {
    }

    public BlockVar(Vector3i position) {
        this.position = position;
    }

    public Vector3i getValue() {
        return position;
    }

    @Override
    public HexVar copy() {
        return new BlockVar(position == null ? null : new Vector3i(position));
    }

    @Override
    public Object getRawValue() {
        return position;
    }

    @Override
    public Double toScalar() {
        return position == null ? 0.0 : 1.0;
    }

    @Override
    public PositionVar toPosition(ComponentAccessor<EntityStore> accessor) {
        if (position == null) return new PositionVar(new Vector3d(0, 0, 0), true);
        return new PositionVar(new Vector3d(position.x + 0.5, position.y + 0.5, position.z + 0.5), true);
    }

    @Override
    public BlockVar toBlockVar(ComponentAccessor<EntityStore> accessor) {
        return this;
    }

    @Override
    public RotationVar toRotation(ComponentAccessor<EntityStore> accessor) {
        if (position == null) return new RotationVar(new Rotation3f());
        try {
            World world = accessor.getExternalData().getWorld();
            int blockId = world.getBlock(position.x, position.y, position.z);
            if (blockId == BlockType.EMPTY_ID) return new RotationVar(new Rotation3f());
            int idx = world.getBlockRotationIndex(position.x, position.y, position.z);
            RotationTuple tuple = RotationTuple.get(idx);
            float yaw = (float) tuple.yaw().getRadians();
            return new RotationVar(new Rotation3f(0f, yaw, 0f));
        } catch (Exception e) {
            return new RotationVar(new Rotation3f());
        }
    }

    @Override
    public HexVar resolveSelf(HexVar partner, ComponentAccessor<EntityStore> accessor) {
        return toPosition(accessor);
    }

    @Override
    public String describe() {
        if (position == null) return "BlockVar: [null]";
        return "BlockVar: (" + position.x + ", " + position.y + ", " + position.z + ")";
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof BlockVar bb) {
            if (position == null || bb.position == null) return position == bb.position;
            return position.equals(bb.position);
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof BlockVar bb) {
            if (position == null && bb.position == null) return 0;
            if (position == null) return -1;
            if (bb.position == null) return 1;
            return Double.compare(this.toScalar(), bb.toScalar());
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return "BlockVar(" + position + ")";
    }

    public static final BuilderCodec<BlockVar> CODEC = BuilderCodec
            .builder(BlockVar.class, BlockVar::new, HexVar.BASE_CODEC)
            .append(new KeyedCodec<>("Position", Vector3iUtil.CODEC),
                    (v, pos) -> v.position = pos,
                    v -> v.position)
            .add()
            .build();

    static {
        HexVar.CODEC.register("Block", BlockVar.class, BlockVar.CODEC);
    }
}
