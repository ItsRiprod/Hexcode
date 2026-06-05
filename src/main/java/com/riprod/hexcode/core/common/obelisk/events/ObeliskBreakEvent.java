package com.riprod.hexcode.core.common.obelisk.events;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.hypixel.hytale.logger.HytaleLogger;

public class ObeliskBreakEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ObeliskBreakEvent() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull BreakBlockEvent event) {

        try {
            if (event.isCancelled())
                return;

            Vector3i pos = event.getTargetBlock();
            World world = buffer.getExternalData().getWorld();

            ObeliskBlockComponent obelisk = BlockModule.getComponent(
                    ObeliskBlockComponent.getComponentType(), world,
                    pos.x, pos.y, pos.z);
            if (obelisk == null) return;

            handleObeliskBreak(world, obelisk, pos);

        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] ObeliskBreakEvent failed: %s", e.getMessage());
        }
    }

    private static void handleObeliskBreak(World world, ObeliskBlockComponent obelisk, Vector3i pos) {
        Vector3i pedestalLoc = obelisk.getRegisteredPedestalLoc();
        if (pedestalLoc == null) {
            return;
        }

        PedestalBlockComponent pedestal = BlockModule.getComponent(
                PedestalBlockComponent.getComponentType(), world,
                pedestalLoc.x, pedestalLoc.y, pedestalLoc.z);
        if (pedestal != null) {
            pedestal.removeObelisk(pos);
        }
        obelisk.clearRegistration();
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
