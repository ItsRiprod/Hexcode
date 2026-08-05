package com.riprod.hexcode.builtin.hexCore.eventListeners;

import java.util.function.Consumer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class GlyphDrawNotificationListener implements Consumer<GlyphDrawnEvent> {

    @Override
    public void accept(GlyphDrawnEvent event) {
        Ref<EntityStore> playerRef = event.getPlayerRef();
        if (playerRef == null || !playerRef.isValid()) return;

        Glyph glyph = event.getGlyph();
        if (glyph == null || glyph.getGlyphId() == null) return;

        Store<EntityStore> store = playerRef.getStore();
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (pr == null) return;

        GlyphAsset asset = event.getMatchedGlyphAsset();
        Message name = asset != null && asset.getTitle() != null
                ? Message.translation(asset.getTitle())
                : Message.raw(glyph.getGlyphId());

        String icon = asset != null ? asset.getIcon() : null;
        NotificationUtil.sendNotification(pr.getPacketHandler(), name, icon);
    }
}
