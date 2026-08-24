package com.riprod.hexcode.core.common.execution.root;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3iUtil;

import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.imbuement.block.BlockImbuementCapacity;

public class BlockHexRoot implements HexRoot {

    private Vector3i blockPos;
    // capacity is derived from blockPos+world at construction; not codec'd.
    // decoded BlockHexRoot has null capacity (out-of-scope for this iteration —
    // persisted hexRoot is round-trip data, not used at runtime by overlay).
    private transient BlockImbuementCapacity.Capacity capacity;

    public BlockHexRoot() {
    }

    public BlockHexRoot(Vector3i blockPos, BlockImbuementCapacity.Capacity capacity) {
        this.blockPos = blockPos;
        this.capacity = capacity;
    }

    public Vector3i getBlockPos() {
        return blockPos;
    }

    public BlockImbuementCapacity.Capacity getCapacity() {
        return capacity;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public Ref<EntityStore> getSourceRef(ComponentAccessor<EntityStore> accessor) {
        return null;
    }

    @Override
    public boolean tryConsumeMana(float cost, ComponentAccessor<EntityStore> accessor) {
        return true;
    }

    @Override
    public float getCurrentMana(ComponentAccessor<EntityStore> accessor) {
        return Float.MAX_VALUE;
    }

    @Override
    public boolean addMana(float amount, ComponentAccessor<EntityStore> accessor) {
        return false;
    }

    @Override
    public HexVar getRootVar(HexContext ctx) {
        return new BlockVar(blockPos);
    }

    public float resolveVolatility() {
        return (float) capacity.getSlots() * capacity.getVolatilityPerSlot();
    }

    public float resolveSpellPower() {
        return 1.0f;
    }

    @Override
    public HexRoot copy() {
        return new BlockHexRoot(blockPos, capacity);
    }

    public static final BuilderCodec<BlockHexRoot> CODEC = BuilderCodec
            .builder(BlockHexRoot.class, BlockHexRoot::new, HexRoot.BASE_CODEC)
            .append(new KeyedCodec<>("BlockPos", Vector3iUtil.CODEC),
                    (c, v) -> c.blockPos = v,
                    c -> c.blockPos)
            .add()
            .build();
}
