package com.riprod.hexcode.core.common.drawing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeEnterEvent;
import com.riprod.hexcode.api.context.DrawModeExitEvent;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.drawing.system.InterfaceManager;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;

// refsystem only fires on entity add/remove; component add/remove on a living player
// dispatches through refchangesystem, so the draw flag lifecycle must live here
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
            Ref<EntityStore> trailRef = capture.getDrawTrailRef();
            if (trailRef != null && trailRef.isValid()) {
                InterfaceManager.removeTrailEntity(buffer, trailRef);
                capture.setDrawTrailRef(null);
            }
            if (ref.isValid()) {
                buffer.invoke(ref, new DrawModeExitEvent(ref));
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] draw mode exit failed");
        }
    }
}
