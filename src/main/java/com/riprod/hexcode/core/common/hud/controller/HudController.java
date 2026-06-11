package com.riprod.hexcode.core.common.hud.controller;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HudController {

    private static final String INFO_ROOT = "#HexcodeInfo";
    private static final String INFO_TITLE = "#HexcodeInfoTitle.TextSpans";
    private static final String INFO_DESCRIPTION = "#HexcodeInfoDescription.TextSpans";

    private HudController() {}

    public static void ensureHud(@Nonnull CommandBuffer<EntityStore> buffer,
                                 @Nonnull Ref<EntityStore> playerEntity) {
        PlayerRef playerRef = buffer.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) return;
        resolve(playerRef);
    }

    public static void showInfo(@Nonnull CommandBuffer<EntityStore> buffer,
                                @Nonnull Ref<EntityStore> playerEntity,
                                @Nullable Message title,
                                @Nullable Message description) {
        PlayerRef playerRef = buffer.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) return;

        HexcodeHud hud = resolve(playerRef);
        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set(INFO_TITLE, title != null ? title : Message.empty());
        cmd.set(INFO_DESCRIPTION, description != null ? description : Message.empty());
        cmd.set(INFO_ROOT + ".Visible", true);
        hud.apply(cmd);
    }

    public static void hideInfo(@Nonnull CommandBuffer<EntityStore> buffer,
                                @Nonnull Ref<EntityStore> playerEntity) {
        PlayerRef playerRef = buffer.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) return;
        HexcodeHud hud = current(playerRef);
        if (hud == null) return;
        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set(INFO_ROOT + ".Visible", false);
        hud.apply(cmd);
    }

    public static void clearHud(@Nonnull CommandBuffer<EntityStore> buffer,
                                @Nonnull Ref<EntityStore> playerEntity) {
        PlayerRef playerRef = buffer.getComponent(playerEntity, PlayerRef.getComponentType());
        if (playerRef == null) return;
        playerRef.getComponent(Player.getComponentType()).getHudManager()
                .removeCustomHud(playerRef, HexcodeHud.KEY);
    }

    @Nonnull
    private static HexcodeHud resolve(@Nonnull PlayerRef playerRef) {
        HudManager manager = playerRef.getComponent(Player.getComponentType()).getHudManager();
        CustomUIHud existing = manager.getCustomHud(HexcodeHud.KEY);
        if (existing instanceof HexcodeHud hud) return hud;
        HexcodeHud fresh = new HexcodeHud(playerRef);
        manager.addCustomHud(playerRef, fresh);
        return fresh;
    }

    @Nullable
    private static HexcodeHud current(@Nonnull PlayerRef playerRef) {
        CustomUIHud existing = playerRef.getComponent(Player.getComponentType())
                .getHudManager().getCustomHud(HexcodeHud.KEY);
        return existing instanceof HexcodeHud hud ? hud : null;
    }
}
