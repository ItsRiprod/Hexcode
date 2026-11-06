package com.riprod.hexcode.core.common.hud.controller;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HudController {

    private HudController() {}

    public static void ensureHud(@Nonnull CommandBuffer<EntityStore> buffer,
                                 @Nonnull Ref<EntityStore> playerEntity) {
        var playerRef = buffer.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) return;
        var manager = hudManager(playerRef);
        if (current(manager) == null) {
            manager.addCustomHud(playerRef, new HexcodeHud(playerRef));
        }
    }

    public static void showInfo(@Nonnull CommandBuffer<EntityStore> buffer,
                                @Nonnull Ref<EntityStore> playerEntity,
                                @Nullable Message title,
                                @Nullable Message description) {
        var playerRef = buffer.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) return;
        var manager = hudManager(playerRef);
        var hud = current(manager);
        if (hud != null) {
            hud.showInfo(title, description);
            return;
        }
        var fresh = new HexcodeHud(playerRef);
        fresh.setInfo(title, description);
        manager.addCustomHud(playerRef, fresh);
    }

    public static void hideInfo(@Nonnull CommandBuffer<EntityStore> buffer,
                                @Nonnull Ref<EntityStore> playerEntity) {
        var playerRef = buffer.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) return;
        var hud = current(hudManager(playerRef));
        if (hud == null) return;
        hud.hideInfo();
    }

    public static void clearHud(@Nonnull CommandBuffer<EntityStore> buffer,
                                @Nonnull Ref<EntityStore> playerEntity) {
        var playerRef = buffer.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) return;
        hudManager(playerRef).removeCustomHud(playerRef, HexcodeHud.KEY);
    }

    @Nonnull
    private static HudManager hudManager(@Nonnull PlayerRef playerRef) {
        return playerRef.getComponent(Player.getComponentType()).getHudManager();
    }

    @Nullable
    private static HexcodeHud current(@Nonnull HudManager manager) {
        return manager.getCustomHud(HexcodeHud.KEY) instanceof HexcodeHud hud ? hud : null;
    }
}
