package com.riprod.hexcode.core.common.glyphs.variables;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3iUtil;

import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
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
    public VarType type() {
        return VarType.TRANSFORM;
    }

    @Override
    public HexVar copy() {
        return new BlockVar(position == null ? null : new Vector3i(position));
    }

    @Override
    public Double scalar() {
        return position == null ? 0.0 : 1.0;
    }

    @Override
    public Vector3d position(ComponentAccessor<EntityStore> accessor) {
        if (position == null)
            return new Vector3d(0, 0, 0);
        return new Vector3d(position.x + 0.5, position.y + 0.5, position.z + 0.5);
    }

    @Override
    public Rotation3f rotation(ComponentAccessor<EntityStore> accessor) {
        if (position == null)
            return new Rotation3f();
        try {
            var world = accessor.getExternalData().getWorld();
            var chunkStore = world.getChunkStore();

            var ref = chunkStore.getChunkSectionReferenceAtBlock(position.x, position.y, position.z);

            if (ref == null || !ref.isValid()) {
                return new Rotation3f();
            }

            var blockSection = chunkStore.getStore().getComponent(ref, BlockSection.getComponentType());
            if (blockSection == null) {
                return new Rotation3f();
            }

            var rot = blockSection.getRotation(position.x, position.y, position.z);

            var euler = new Rotation3f(
                    radians(rot.pitch()),
                    radians(rot.yaw()),
                    radians(rot.roll())
                );
                
            return euler;

        } catch (Exception e) {
            return new Rotation3f();
        }
    }

    // wrapped so block rotations compare against entity rotations, which are always
    // (-PI, PI]
    private static float radians(Rotation rotation) {
        return MathUtil.wrapAngle((float) rotation.getRadians());
    }

    @Override
    public String describe() {
        if (position == null)
            return "BlockVar: [null]";
        return "BlockVar: (" + position.x + ", " + position.y + ", " + position.z + ")";
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof BlockVar bb) {
            if (position == null || bb.position == null)
                return position == bb.position;
            return position.equals(bb.position);
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof BlockVar bb) {
            if (position == null && bb.position == null)
                return 0;
            if (position == null)
                return -1;
            if (bb.position == null)
                return 1;
            return Double.compare(this.scalar(), bb.scalar());
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
