package com.riprod.hexcode.core.common.drawing;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;

public class DrawAnchorSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return DrawCaptureComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            DrawCaptureComponent capture = chunk.getComponent(index, DrawCaptureComponent.getComponentType());
            if (capture == null || capture.getDraggingHex() == null) {
                return;
            }
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            DrawAnchorUtils.ensureAnchor(buffer, player, capture);
            HeadRotation head = chunk.getComponent(index, HeadRotation.getComponentType());
            DrawAnchorUtils.rotateToHead(buffer, capture, head);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] draw anchor tick failed");
        }
    }
}
