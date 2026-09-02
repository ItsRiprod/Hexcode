package com.riprod.hexcode.builtin.hexCore.contexts.crafting.utils;

import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorActivateButtonEvent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotificationType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.GlyphPlaceEvent;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class CraftingGlyphEditorSpawn {

    public static final String BUTTON_ID = "HexcodeSpawnGlyph";
    public static final String BUTTON_TEXT_ID = "server.hexcode.assetEditor.buttons.spawnGlyph";

    private static final String MESSAGE_PREFIX = "server.hexcode.assetEditor.messages.spawnGlyph.";
    private static final float SPAWN_QUALITY = 1.0f;

    private CraftingGlyphEditorSpawn() {
    }

    public static void activate(AssetEditorActivateButtonEvent event) {
        EditorClient client = event.getEditorClient();
        AssetPath open = AssetEditorPlugin.get().getOpenAssetPath(client);
        if (open == null || open.path().toString().isEmpty()) {
            return;
        }

        String glyphId = GlyphAsset.getAssetStore().decodeFilePathKey(open.path());
        GlyphAsset asset = glyphId != null ? GlyphAsset.getAssetMap().getAsset(glyphId) : null;
        if (asset == null) {
            return;
        }

        PlayerRef playerRef = client.tryGetPlayer();
        if (playerRef == null) {
            notify(client, AssetEditorPopupNotificationType.Warning, "noGameClient", asset.getId());
            return;
        }

        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }

        Store<EntityStore> store = ref.getStore();
        store.getExternalData().getWorld().execute(() -> spawn(client, store, ref, asset));
    }

    private static void spawn(EditorClient client, Store<EntityStore> store, Ref<EntityStore> playerRef,
            GlyphAsset asset) {
        if (!playerRef.isValid()) {
            return;
        }

        CasterComponent caster = store.getComponent(playerRef, CasterComponent.getComponentType());
        if (caster == null || caster.getCurrentContext() == null) {
            notify(client, AssetEditorPopupNotificationType.Warning, "noContext", asset.getId());
            return;
        }

        Glyph glyph = new Glyph(asset, SPAWN_QUALITY, SPAWN_QUALITY);
        store.invoke(playerRef, new GlyphPlaceEvent(playerRef, glyph, asset, null));
        notify(client, AssetEditorPopupNotificationType.Success, "spawned", asset.getId());
    }

    private static void notify(EditorClient client, AssetEditorPopupNotificationType type, String key,
            String glyphId) {
        client.sendPopupNotification(type, Message.translation(MESSAGE_PREFIX + key).param("id", glyphId));
    }
}
