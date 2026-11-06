package com.riprod.hexcode.builtin.hexCore.obelisks;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

public enum ObeliskStatus {
    NORMAL("StatusStyle"),
    WARNING("WarningStatusStyle"),
    ERROR("ErrorStatusStyle");

    private static final String STYLE_DOC = "Hexcode/Obelisks/Obelisk.ui";
    private static final String LABEL = "#StatusLabel";

    private final Value<String> style;

    ObeliskStatus(@Nonnull String styleName) {
        this.style = Value.ref(STYLE_DOC, styleName);
    }

    public void write(@Nonnull UICommandBuilder cmd, @Nonnull Message message) {
        cmd.set(LABEL + ".TextSpans", message);
        cmd.set(LABEL + ".Style", style);
    }
}
