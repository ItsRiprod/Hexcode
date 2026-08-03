package com.riprod.hexcode.core.common.imbuement.block;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.World;
import com.riprod.hexcode.core.common.imbuement.asset.EssenceAsset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class EssenceRefill {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final double HORIZONTAL_RADIUS = 2.0;
    private static final double VERTICAL_RADIUS = 2.0;

    private EssenceRefill() {
    }

    @Nullable
    public static EssenceAsset tryConsume(@Nonnull World world, @Nonnull Vector3i blockPos) {
        Store<ChunkStore> store = world.getChunkStore().getStore();
        SpatialResource<Ref<ChunkStore>, ChunkStore> idx =
                store.getResource(BlockModule.get().getItemContainerSpatialResourceType());

        Vector3d center = new Vector3d(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5);
        List<Ref<ChunkStore>> results = SpatialResource.getThreadLocalReferenceList();
        idx.getSpatialStructure().ordered3DAxis(center,
                HORIZONTAL_RADIUS, VERTICAL_RADIUS, HORIZONTAL_RADIUS, results);

        LOGGER.atFine().log("essence-refill: spatial scan around %s returned %d container(s)",
                blockPos, results.size());

        if (results.isEmpty()) return null;

        for (Ref<ChunkStore> ref : results) {
            if (!ref.isValid()) continue;
            ItemContainerBlock chest = store.getComponent(ref, ItemContainerBlock.getComponentType());
            if (chest == null) continue;
            ItemContainer inv = chest.getItemContainer();
            if (inv == null) continue;

            short capacity = inv.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                ItemStack stack = inv.getItemStack(slot);
                if (stack == null || stack.isEmpty()) continue;

                String itemId = stack.getItemId();
                EssenceAsset essence = EssenceAsset.getAssetMap().getAsset(itemId);
                LOGGER.atFine().log("essence-refill: slot %d itemId=%s essenceMatch=%s",
                        slot, itemId, essence != null ? essence.getId() : "null");
                if (essence == null) continue;

                inv.removeItemStackFromSlot(slot, 1);
                LOGGER.atFine().log("essence-refill: consumed %s from container", essence.getId());
                return essence;
            }
        }
        return null;
    }
}
