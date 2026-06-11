package com.riprod.hexcode.core.common.pedestal.events;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.HytaleServer;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskSystem;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.state.crafting.constants.PedestalState;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;

public class PedestalBlockEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public PedestalBlockEvent() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull BreakBlockEvent event) {

        try {
            if (event.isCancelled()) return;

            Vector3i pos = event.getTargetBlock();
            World world = buffer.getExternalData().getWorld();

            PedestalBlockComponent pedestal = BlockModule.getComponent(
                    PedestalBlockComponent.getComponentType(), world,
                    pos.x, pos.y, pos.z);
            if (pedestal == null) return;

            Ref<EntityStore> sessionRef = SessionUtils.getSessionRef(pedestal);
            if (sessionRef != null) {
                HexcodeSessionComponent session = buffer.getComponent(sessionRef,
                        HexcodeSessionComponent.getComponentType());
                if (session != null && session.getOwnerRef() != null) {
                    HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                            .dispatch(CraftingEvent.builder(CraftingEvent.Reason.EXITED_PEDESTAL_BROKEN,
                                    session.getOwnerRef())
                                    .pedestal(pedestal)
                                    .message("The pedestal was destroyed.")
                                    .build());
                }
                SessionUtils.endSession(buffer, sessionRef, world);
            }

            Ref<EntityStore> anchorRef = pedestal.getAnchorRef();
            if (anchorRef != null && anchorRef.isValid()) {
                buffer.removeEntity(anchorRef, RemoveReason.REMOVE);
                pedestal.setAnchorRef(null);
            }

            ObeliskSystem.updateState(buffer, pedestal, world, PedestalState.SELECTING, PedestalState.IDLE);

        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] PedestalBlockEvent failed: %s", e.getMessage());
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
