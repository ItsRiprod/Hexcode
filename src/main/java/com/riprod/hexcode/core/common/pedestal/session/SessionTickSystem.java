package com.riprod.hexcode.core.common.pedestal.session;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SessionTickSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return HexcodeSessionComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index, ArchetypeChunk<EntityStore> chunk,
            Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {

        try {
            Ref<EntityStore> ownerRef = chunk.getReferenceTo(index);
            HexcodeSessionComponent session = chunk.getComponent(index,
                    HexcodeSessionComponent.getComponentType());

            if (session.getOwnerRef() == null) {
                session.setOwnerRef(ownerRef);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] SessionTickSystem failed: %s", e.getMessage());
        }
    }
}
