package com.riprod.hexcode.core.common.imbuement.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.riprod.hexcode.utils.BlockAccess;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedBlockComponent;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;

public class ImbuedBlockBreakHandler extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ImbuedBlockBreakHandler() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull BreakBlockEvent event) {
        try {
            Vector3i pos = event.getTargetBlock();
            World world = buffer.getExternalData().getWorld();

            ImbuedBlockComponent comp = BlockModule.getComponent(
                    ImbuedBlockComponent.getComponentType(), world, pos.x, pos.y, pos.z);
            if (comp == null) return;
            if (comp.getSlots().isEmpty()) return;

            BlockType blockType = event.getBlockType();
            String dropItemId = resolveDropItemId(blockType);
            if (dropItemId == null) {
                LOGGER.atWarning().log("imbued-break: no drop item id for blockType=%s; letting engine handle (drop will be bare)",
                        blockType.getId());
                return;
            }

            event.setCancelled(true);

            ItemStack drop = ImbuementUtils.writeAll(new ItemStack(dropItemId), comp.getSlots());
            Vector3d dropPos = new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);

            world.execute(() -> BlockAccess.clearBlock(world, pos.x, pos.y, pos.z,
                    SetBlockSettings.PERFORM_BLOCK_UPDATE));

            Holder<EntityStore> dropHolder = ItemComponent.generateItemDrop(
                    buffer, drop, dropPos, new Rotation3f(), 0f, 0f, 0f);
            if (dropHolder == null) return;
            buffer.addEntity(dropHolder, AddReason.SPAWN);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] ImbuedBlockBreakHandler failed: %s", e.getMessage());
        }
    }

    @Nullable
    private static String resolveDropItemId(@Nonnull BlockType blockType) {
        if (blockType.getGathering() != null && blockType.getGathering().getBreaking() != null) {
            String id = blockType.getGathering().getBreaking().getItemId();
            if (id != null && !id.isEmpty()) return id;
        }
        Item item = blockType.getItem();
        if (item != null) {
            String id = item.getId();
            if (id != null && !id.isEmpty()) return id;
        }
        return null;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
