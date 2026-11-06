package com.riprod.hexcode.core.common.hud.controller;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HexcodeHud extends CustomUIHud {

    public static final String KEY = "hexcode:main";
    public static final String SHELL_DOC = "Hexcode/Hud/Shell.ui";

    private static final String INFO_VISIBLE = "#HexcodeInfo.Visible";
    private static final String INFO_TITLE = "#HexcodeInfoTitle.TextSpans";
    private static final String INFO_DESCRIPTION = "#HexcodeInfoDescription.TextSpans";
    private static final String INFO_DESCRIPTION_VISIBLE = "#HexcodeInfoDescription.Visible";

    @Nullable
    private Message infoTitle;
    @Nullable
    private Message infoDescription;
    private boolean infoVisible;

    public HexcodeHud(@Nonnull PlayerRef playerRef) {
        super(playerRef, KEY);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder cmd) {
        cmd.append(SHELL_DOC);
        writeInfo(cmd);
    }

    public void setInfo(@Nullable Message title, @Nullable Message description) {
        infoTitle = title;
        infoDescription = description;
        infoVisible = true;
    }

    public void showInfo(@Nullable Message title, @Nullable Message description) {
        setInfo(title, description);
        var cmd = new UICommandBuilder();
        writeInfo(cmd);
        update(false, cmd);
    }

    public void hideInfo() {
        infoVisible = false;
        var cmd = new UICommandBuilder();
        cmd.set(INFO_VISIBLE, false);
        update(false, cmd);
    }

    private void writeInfo(@Nonnull UICommandBuilder cmd) {
        cmd.set(INFO_TITLE, infoTitle != null ? infoTitle : Message.empty());
        cmd.set(INFO_DESCRIPTION, infoDescription != null ? infoDescription : Message.empty());
        cmd.set(INFO_DESCRIPTION_VISIBLE, infoDescription != null);
        cmd.set(INFO_VISIBLE, infoVisible);
    }
}
