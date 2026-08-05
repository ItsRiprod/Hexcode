package com.riprod.hexcode.core.common.glyphs.icon;

import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorActivateButtonEvent;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotificationType;
import com.hypixel.hytale.server.core.Message;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class GlyphIconEditorButton {

    public static final String BUTTON_ID = "HexcodeRegenerateGlyphIcon";
    public static final String BUTTON_TEXT_ID = "server.hexcode.assetEditor.buttons.regenerateGlyphIcon";

    private static final String MESSAGE_PREFIX = "server.hexcode.assetEditor.messages.glyphIcon.";

    private GlyphIconEditorButton() {
    }

    public static void activate(AssetEditorActivateButtonEvent event, PluginManifest manifest) {
        EditorClient client = event.getEditorClient();
        AssetPath open = AssetEditorPlugin.get().getOpenAssetPath(client);
        if (open == null || open.path().toString().isEmpty()) {
            return;
        }

        String glyphId = GlyphAsset.getAssetStore().decodeFilePathKey(open.path());
        if (glyphId == null) {
            return;
        }

        GlyphIconStore.RegenerateStatus status = GlyphIconStore.regenerate(glyphId, manifest);
        if (status == GlyphIconStore.RegenerateStatus.OK) {
            GlyphEditorPreview.push(client, open);
            if (isOverridden(glyphId)) {
                notify(client, AssetEditorPopupNotificationType.Warning, "overridden", glyphId);
                return;
            }
        }
        notifyResult(client, status, glyphId);
    }

    private static boolean isOverridden(String glyphId) {
        GlyphAsset glyph = GlyphAsset.getAssetMap().getAsset(glyphId);
        return glyph != null && !GlyphIconStore.derivedIconPath(glyphId).equals(glyph.getIcon());
    }

    private static void notifyResult(EditorClient client, GlyphIconStore.RegenerateStatus status,
            String glyphId) {
        switch (status) {
            case OK -> notify(client, AssetEditorPopupNotificationType.Success, "regenerated", glyphId);
            case UNKNOWN_GLYPH -> notify(client, AssetEditorPopupNotificationType.Error, "unknownGlyph", glyphId);
            case NOT_RENDERABLE -> notify(client, AssetEditorPopupNotificationType.Warning, "notRenderable", glyphId);
            case NO_TARGET -> notify(client, AssetEditorPopupNotificationType.Error, "noWritablePack", glyphId);
            case WRITE_FAILED -> notify(client, AssetEditorPopupNotificationType.Error, "writeFailed", glyphId);
        }
    }

    private static void notify(EditorClient client, AssetEditorPopupNotificationType type, String key,
            String glyphId) {
        client.sendPopupNotification(type, Message.translation(MESSAGE_PREFIX + key).param("id", glyphId));
    }
}
