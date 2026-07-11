package com.riprod.hexcode.core.common.drawing;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeEnterEvent;
import com.riprod.hexcode.api.context.DrawModeExitEvent;
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.drawing.system.InterfaceManager;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.drawing.utils.DraftFeedback;
import com.riprod.hexcode.utils.CleanupUtils;

public class DrawModeLifecycleSystem extends RefChangeSystem<EntityStore, DrawCaptureComponent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public ComponentType<EntityStore, DrawCaptureComponent> componentType() {
        return DrawCaptureComponent.getComponentType();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return DrawCaptureComponent.getComponentType();
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DrawCaptureComponent capture,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            capture.setPalette(CasterInventory.getHexesForCasting(buffer, ref));
            buffer.invoke(new DrawModeEnterEvent(ref, capture.getPalette()));
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] draw mode enter failed");
        }
    }

    @Override
    public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable DrawCaptureComponent oldCapture,
            @Nonnull DrawCaptureComponent newCapture, @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> buffer) {
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull DrawCaptureComponent capture,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            if (!ref.isValid()) {
                return;
            }
            if (capture.isStrokeActive()) {
                DrawCaptureService.endStroke(buffer, ref, capture, resolveUuid(buffer, ref));
            }
            Ref<EntityStore> trailRef = capture.getDrawTrailRef();
            if (trailRef != null && trailRef.isValid()) {
                InterfaceManager.removeTrailEntity(buffer, trailRef);
                capture.setDrawTrailRef(null);
            }

            CleanupUtils.safeRemoveEntities(buffer, capture.getPersistentStrokeRefs());
            capture.getPersistentStrokeRefs().clear();
            DrawAnchorUtils.removeAnchor(buffer, capture);

            buffer.invoke(ref, new DrawModeExitEvent(ref));
            
            if (capture.getPendingShapes().isEmpty()) {
                return;
            }
            CasterComponent caster = buffer.getComponent(ref, CasterComponent.getComponentType());
            String contextId = caster != null ? caster.getCurrentContext() : null;
            ShapeStructure structure = DrawCaptureService.computeStructure(capture.getPendingShapes());
            capture.getPendingShapes().clear();
            capture.setFinalizePending(false);
            if (structure == null) {
                return;
            }
            if (contextId == null) {
                DraftFeedback.playFailFeedback(buffer, ref);
                return;
            }
            buffer.invoke(ref, new ShapeDrawnEvent(ref, structure));
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] draw mode exit failed");
        }
    }

    @Nullable
    private static UUID resolveUuid(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        UUIDComponent uuid = buffer.getComponent(player, UUIDComponent.getComponentType());
        return uuid != null ? uuid.getUuid() : null;
    }
}
