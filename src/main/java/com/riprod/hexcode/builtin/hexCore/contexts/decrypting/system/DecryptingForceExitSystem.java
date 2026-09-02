package com.riprod.hexcode.builtin.hexCore.contexts.decrypting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.common.ContextForceExitEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.component.DecryptingState;
import com.riprod.hexcode.core.common.context.ContextTransitionService;

public class DecryptingForceExitSystem extends EntityEventSystem<EntityStore, ContextForceExitEvent> {

    public DecryptingForceExitSystem() {
        super(ContextForceExitEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return DecryptingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ContextForceExitEvent event) {
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        ContextTransitionService.exit(buffer, player, DecryptingState.CONTEXT_ID);
    }
}
