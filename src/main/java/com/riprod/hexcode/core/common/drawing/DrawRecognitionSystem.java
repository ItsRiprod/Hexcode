package com.riprod.hexcode.core.common.drawing;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.drawing.utils.DraftFeedback;

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
            if (capture == null) {
                return;
            }

            boolean forceCommit = consumeForceCommit(chunk, index, capture, buffer);
            if (!capture.isFinalizePending()) {
                return;
            }

            if (!forceCommit) {
                if (capture.getFinalizeDelaySeconds() < 0f) {
                    return;
                }
                float timer = capture.getFinalizeTimer() + dt;
                if (timer < capture.getFinalizeDelaySeconds()) {
                    capture.setFinalizeTimer(timer);
                    return;
                }
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

    private static boolean consumeForceCommit(ArchetypeChunk<EntityStore> chunk, int index,
            DrawCaptureComponent capture, CommandBuffer<EntityStore> buffer) {
        CasterComponent caster = chunk.getComponent(index, CasterComponent.getComponentType());
        if (caster == null) {
            return false;
        }
        InteractionType ability = caster.consumeAbilityPressed();
        if (ability == null) {
            return false;
        }
        if (ability != InteractionType.Ability2) {
            caster.pressAbility(ability);
            return false;
        }

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (capture.isStrokeActive()) {
            DrawCaptureService.endStroke(buffer, ref, capture, resolveUuid(buffer, ref));
        }
        if (!capture.getPendingShapes().isEmpty()) {
            capture.setFinalizePending(true);
        }
        return true;
    }

    @Nullable
    private static UUID resolveUuid(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        UUIDComponent uuid = buffer.getComponent(player, UUIDComponent.getComponentType());
        return uuid != null ? uuid.getUuid() : null;
    }
}
