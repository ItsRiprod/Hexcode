package com.riprod.hexcode.core.common.obelisk.system;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;

import org.joml.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.obelisk.interfaces.ObeliskInterface;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;

public class ObeliskDispatcher {
    private static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ObeliskDispatcher() {
    }

    public static void dispatchStateChange(CommandBuffer<EntityStore> buffer,
            PedestalBlockComponent pedestal, PedestalState previousState, PedestalState newState) {
        List<Vector3i> obelisks = pedestal.getActiveObelisks();
        if (obelisks.isEmpty()) return;

        for (Vector3i pos : obelisks) {
            if (pos == null) continue;

            ObeliskBlockComponent obelisk = BlockModule.getComponent(
                    ObeliskBlockComponent.getComponentType(),
                    buffer.getExternalData().getWorld(),
                    pos.x, pos.y, pos.z);
            if (obelisk == null) continue;

            String handlerId = obelisk.getHandlerId();
            if (handlerId == null || handlerId.isEmpty()) continue;

            ObeliskInterface handler = ObeliskHandlerRegistry.get(handlerId);
            if (handler == null) continue;

            handler.onStateChange(buffer, obelisk, pos, previousState, newState);
        }
    }

    public static void dispatchGlyphDrawn(ComponentAccessor<EntityStore> buffer,
            PedestalBlockComponent pedestal, Ref<EntityStore> playerRef, Glyph glyph) {
        forEachHandler(buffer, pedestal, (handler, obelisk) ->
                handler.onGlyphDrawn(buffer, playerRef, glyph, obelisk));
    }

    public static void dispatchEnterCrafting(CommandBuffer<EntityStore> buffer,
            PedestalBlockComponent pedestal, Ref<EntityStore> playerRef) {
        forEachHandler(buffer, pedestal, (handler, obelisk) ->
                handler.onEnterCrafting(buffer, playerRef, obelisk));
    }

    public static void dispatchExitCrafting(CommandBuffer<EntityStore> buffer,
            PedestalBlockComponent pedestal, Ref<EntityStore> playerRef) {
        forEachHandler(buffer, pedestal, (handler, obelisk) ->
                handler.onExitCrafting(buffer, playerRef, obelisk));
    }

    public static void dispatchHover(CommandBuffer<EntityStore> buffer,
            PedestalBlockComponent pedestal, Ref<EntityStore> playerRef, Ref<EntityStore> hoveredRef) {
        forEachHandler(buffer, pedestal, (handler, obelisk) ->
                handler.onHover(buffer, playerRef, hoveredRef, obelisk));
    }

    public static void dispatchUnhover(CommandBuffer<EntityStore> buffer,
            PedestalBlockComponent pedestal, Ref<EntityStore> playerRef, Ref<EntityStore> unhoveredRef) {
        forEachHandler(buffer, pedestal, (handler, obelisk) ->
                handler.onUnhover(buffer, playerRef, unhoveredRef, obelisk));
    }

    private static void forEachHandler(ComponentAccessor<EntityStore> buffer,
            PedestalBlockComponent pedestal, ObeliskCallback callback) {
        List<Vector3i> obelisks = pedestal.getActiveObelisks();
        if (obelisks.isEmpty()) return;

        for (Vector3i pos : obelisks) {
            if (pos == null) continue;

            ObeliskBlockComponent obelisk = BlockModule.getComponent(
                    ObeliskBlockComponent.getComponentType(),
                    buffer.getExternalData().getWorld(),
                    pos.x, pos.y, pos.z);
            if (obelisk == null) continue;

            String handlerId = obelisk.getHandlerId();
            if (handlerId == null || handlerId.isEmpty()) continue;

            ObeliskInterface handler = ObeliskHandlerRegistry.get(handlerId);
            if (handler == null) continue;

            callback.accept(handler, obelisk);
        }
    }

    @FunctionalInterface
    private interface ObeliskCallback {
        void accept(ObeliskInterface handler, ObeliskBlockComponent obelisk);
    }
}
