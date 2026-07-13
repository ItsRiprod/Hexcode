package com.riprod.hexcode.state;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexStateChangeEvent;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;

public class HexTick extends EntityTickingSystem<EntityStore> {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  @Override
  public Query<EntityStore> getQuery() {
    return HexcasterComponent.getComponentType();
  }

  @Override
  public void tick(float dt, int index, ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store, CommandBuffer<EntityStore> buffer) {
    try {
      HexcasterComponent comp = chunk.getComponent(index,
          HexcasterComponent.getComponentType());

      Ref<EntityStore> ref = chunk.getReferenceTo(index);

      HexState pending = comp.consumePendingState();
      if (pending != null) {
        HexState current = comp.getState();
        LOGGER.atFine().log("%s -> %s", current, pending);
        HexcodeManager old = StateRouter.route(current);
        if (old != null) {
          old.lastTick(ref, comp, store, buffer, pending);
        }

        comp.applyState(pending);

             HytaleServer.get().getEventBus().dispatchFor(HexStateChangeEvent.class)
                .dispatch(new HexStateChangeEvent(ref, current, pending));

        HexcodeManager next = StateRouter.route(pending);
        if (next != null) {
          next.firstTick(ref, comp, store, buffer, current);
          return;
        }
      }

      HexcodeManager manager = StateRouter.route(comp.getState());
      if (manager != null) {
        manager.tick(ref, comp, dt, store, buffer);
      }
    } catch (Exception e) {
      LOGGER.atSevere().withCause(e).log("[hexcode] HexTick failed: %s", e.getMessage());
    }
  }
}
