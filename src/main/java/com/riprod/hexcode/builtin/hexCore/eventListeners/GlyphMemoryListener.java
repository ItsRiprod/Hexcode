package com.riprod.hexcode.builtin.hexCore.eventListeners;

import java.util.function.Consumer;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.component.PlayerMemories;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.memories.GlyphMemory;

public class GlyphMemoryListener implements Consumer<GlyphDrawnEvent> {

    private static final String ICON = "NotificationIcons/MemoriesIcon.png";

    @Override
    public void accept(GlyphDrawnEvent event) {
        MemoriesPlugin memories = MemoriesPlugin.get();
        if (memories == null) return;

        Ref<EntityStore> playerRef = event.getPlayerRef();
        if (playerRef == null || !playerRef.isValid()) return;

        Glyph glyph = event.getGlyph();
        if (glyph == null || glyph.getGlyphId() == null) return;

        Store<EntityStore> store = playerRef.getStore();
        PlayerMemories playerMemories = store.getComponent(playerRef, PlayerMemories.getComponentType());
        if (playerMemories == null) return; // memories feature not unlocked for this player

        GlyphMemory memory = new GlyphMemory(glyph.getGlyphId());
        memory.setCapturedTimestamp(System.currentTimeMillis());
        if (memories.hasRecordedMemory(memory)) return;
        if (!playerMemories.recordMemory(memory)) return;

        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (pr == null) return;

        Message collected = Message.translation("hexcode.memories.glyph.collected")
                .param("memoryTitle", Message.translation(memory.getTitle()));
        NotificationUtil.sendNotification(pr.getPacketHandler(), collected, null, ICON);
    }
}
