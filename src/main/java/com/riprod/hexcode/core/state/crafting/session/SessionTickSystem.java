package com.riprod.hexcode.core.state.crafting.session;

import java.util.List;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.node.NodeRouter;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.state.crafting.constants.PedestalState;

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

            drainPendingReenter(buffer, session);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] SessionTickSystem failed: %s", e.getMessage());
        }
    }

    private static void drainPendingReenter(CommandBuffer<EntityStore> buffer,
            HexcodeSessionComponent session) {

        String pendingKey = session.getPendingReenterSlotKey();
        if (pendingKey == null) return;
        if (session.getState() != PedestalState.SELECTING) return;

        Vector3i pedestalLoc = session.getPedestalLocation();
        Ref<EntityStore> ownerRef = session.getOwnerRef();
        if (pedestalLoc == null || ownerRef == null) return;

        World world = buffer.getExternalData().getWorld();
        PedestalBlockComponent pedestal = BlockModule.getComponent(
                PedestalBlockComponent.getComponentType(), world,
                pedestalLoc.x, pedestalLoc.y, pedestalLoc.z);
        if (pedestal == null) return;

        Ref<EntityStore> previewRef = findPreviewBySlotKey(buffer, session, pendingKey);
        if (previewRef == null || !previewRef.isValid()) return;

        session.setPendingReenterSlotKey(null);
        NodeRouter.enter(buffer, previewRef, ownerRef);
    }

    private static Ref<EntityStore> findPreviewBySlotKey(CommandBuffer<EntityStore> buffer,
            HexcodeSessionComponent session, String slotKey) {

        List<Ref<EntityStore>> previews = session.getHexPreviewRefs();
        if (previews == null) return null;
        for (Ref<EntityStore> ref : previews) {
            if (ref == null || !ref.isValid()) continue;
            SlotComponent slotRef = buffer.getComponent(ref,
                    SlotComponent.getComponentType());
            if (slotRef != null && slotKey.equals(slotRef.getSlotKey())) {
                return ref;
            }
        }
        return null;
    }
}
