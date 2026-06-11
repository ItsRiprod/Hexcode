package com.riprod.hexcode.builtin.obelisks.seeker;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.hud.controller.HudController;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.obelisk.interfaces.ObeliskInterface;

public class SeekerObelisk implements ObeliskInterface {
    private static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void onEnterCrafting(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            ObeliskBlockComponent obelisk) {
        HudController.ensureHud(buffer, playerRef);
    }

    @Override
    public void onHover(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            Ref<EntityStore> hoveredRef, ObeliskBlockComponent obelisk) {
        DisplayNameComponent displayName = buffer.getComponent(hoveredRef, DisplayNameComponent.getComponentType());
        Message title = displayName != null ? displayName.getDisplayName() : null;

        HoverableComponent hoverable = buffer.getComponent(hoveredRef, HoverableComponent.getComponentType());
        Message description = hoverable != null ? hoverable.getHintText("description") : null;

        LOGGER.atInfo().log("Showing hover for Seeker Obelisk. Title: %s, Description: %s", title, description);
        HudController.showInfo(buffer, playerRef, title, description);
    }

    @Override
    public void onUnhover(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            Ref<EntityStore> unhoveredRef, ObeliskBlockComponent obelisk) {
        LOGGER.atInfo().log("Hiding hover for Seeker Obelisk.");
        HudController.hideInfo(buffer, playerRef);
    }

    @Override
    public void onExitCrafting(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            ObeliskBlockComponent obelisk) {
        LOGGER.atInfo().log("Exiting crafting for Seeker Obelisk. Tearing down HUD.");
        HudController.clearHud(buffer, playerRef);
    }
}
