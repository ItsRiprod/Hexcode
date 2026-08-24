package com.riprod.hexcode.core.common.pedestal.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;

public class PedestalPlaceEvent extends RefSystem<ChunkStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nullable
    @Override
    public Query<ChunkStore> getQuery() {
        return PedestalBlockComponent.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {

        PedestalBlockComponent pedestal = commandBuffer.getComponent(ref, PedestalBlockComponent.getComponentType());

        var blockStateInfo = commandBuffer.getComponent(ref,
                BlockModule.BlockStateInfo.getComponentType());

        if (pedestal == null || blockStateInfo == null) {
            LOGGER.atWarning().log("Pedestal placed but %s is null for ref=%s",
                    pedestal == null ? "PedestalBlockComponent" : "BlockStateInfo",
                    ref);
            return;
        }

        Ref<ChunkStore> chunkRef = blockStateInfo.getSectionRef();
        if (!chunkRef.isValid()) {
            LOGGER.atWarning().log("Pedestal placed but sectionRef is invalid for ref=%s", ref);
            return;
        }

        var blockChunk = commandBuffer.getComponent(chunkRef, BlockSection.getComponentType());
        if (blockChunk == null) {
            LOGGER.atWarning().log("Pedestal placed but BlockChunk is null for sectionRef=%s", chunkRef);
            return;
        }

        Vector3i blockPos = new Vector3i();
        if (!blockStateInfo.fillWorldPos(commandBuffer, blockPos)) {
            LOGGER.atWarning().log("Pedestal placed but world position is unresolvable for ref=%s", ref);
            return;
        }

        pedestal.setLocation(blockPos);

        World world = commandBuffer.getExternalData().getWorld();
        Store<EntityStore> entityStore = world.getEntityStore().getStore();
        Holder<EntityStore> anchorHolder = PedestalEntity.buildAnchorHolder(entityStore, blockPos);
        if (anchorHolder == null) {
            return;
        }

        Ref<EntityStore> anchorRef = new Ref<>(entityStore);
        pedestal.setAnchorRef(anchorRef);
        world.execute(() -> entityStore.addEntity(anchorHolder, anchorRef, AddReason.SPAWN));
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
    }
}
