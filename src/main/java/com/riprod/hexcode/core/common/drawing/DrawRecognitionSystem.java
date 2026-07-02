package com.riprod.hexcode.core.common.drawing;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.state.casting.utils.DraftFeedback;

public class DrawRecognitionSystem extends EntityTickingSystem<EntityStore> {
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
            if (capture == null || !capture.isFinalizePending()) {
                return;
            }

            float timer = capture.getFinalizeTimer() + dt;
            if (timer < capture.getFinalizeDelaySeconds()) {
                capture.setFinalizeTimer(timer);
                return;
            }
            capture.setFinalizePending(false);
            capture.setFinalizeTimer(0f);

            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            ShapeStructure structure = DrawCaptureService.computeStructure(capture.getPendingShapes());
            capture.getPendingShapes().clear();
            if (structure == null) {
                return;
            }

            CasterComponent caster = chunk.getComponent(index, CasterComponent.getComponentType());
            String contextId = caster != null ? caster.getCurrentContext() : null;
            if (contextId == null) {
                DraftFeedback.playFailFeedback(buffer, ref);
                return;
            }

            buffer.invoke(ref, new ShapeDrawnEvent(ref, structure));
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] draw recognition failed");
        }
    }
}
