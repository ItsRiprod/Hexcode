package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeExitEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.FlycastingCommit;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class FlycastingExitSystem extends EntityEventSystem<EntityStore, DrawModeExitEvent> {

    public FlycastingExitSystem() {
        super(DrawModeExitEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return FlycastingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull DrawModeExitEvent event) {
        FlycastingState state = chunk.getComponent(index, FlycastingState.getComponentType());
        if (state == null) {
            return;
        }
        Ref<EntityStore> player = chunk.getReferenceTo(index);

        // a preempting context already rewrote the caster; forfeited strokes must
        // never queue for execution
        CasterComponent caster = chunk.getComponent(index, CasterComponent.getComponentType());
        if (caster == null || !FlycastingState.CONTEXT_ID.equals(caster.getCurrentContext())) {
            return;
        }
        DrawCaptureComponent capture = chunk.getComponent(index, DrawCaptureComponent.getComponentType());

        Hex queued = FlycastingCommit.finalizeDraft(buffer, player, capture);
        if (queued == null && state.getLastHoveredHex() != null) {
            queued = state.getLastHoveredHex().getHex();
        }
        if (queued != null) {
            ExecutionComponent execution = buffer.ensureAndGetComponent(player,
                    ExecutionComponent.getComponentType());
            execution.setQueuedHex(queued);
        }

        ContextTransitionService.exit(buffer, player, FlycastingState.CONTEXT_ID);
    }
}
