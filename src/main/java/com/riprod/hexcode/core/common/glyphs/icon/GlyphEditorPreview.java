package com.riprod.hexcode.core.common.glyphs.icon;

import org.joml.Vector3f;

import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorSelectAssetEvent;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPreviewCameraSettings;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateModelPreview;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphModelUtil;

public final class GlyphEditorPreview {

    private static final String ASSET_TYPE = GlyphAsset.class.getSimpleName();
    private static final float PREVIEW_SCALE = 1.0f;

    private static final AssetEditorPreviewCameraSettings CAMERA = new AssetEditorPreviewCameraSettings(
            0.25f, new Vector3f(0, 75, 0), new Vector3f(0, (float) Math.toRadians(45), 0));

    private GlyphEditorPreview() {
    }

    public static void onSelectAsset(AssetEditorSelectAssetEvent event) {
        if (!ASSET_TYPE.equals(event.getAssetType())) {
            return;
        }
        push(event.getEditorClient(), event.getAssetFilePath());
    }

    public static void push(EditorClient client, AssetPath path) {
        if (path == null || path.path().toString().isEmpty()) {
            return;
        }

        String glyphId = GlyphAsset.getAssetStore().decodeFilePathKey(path.path());
        GlyphAsset glyph = glyphId != null ? GlyphAsset.getAssetMap().getAsset(glyphId) : null;
        if (glyph == null) {
            return;
        }

        Model model = GlyphModelUtil.assemble(glyph, null, PREVIEW_SCALE);
        if (model == null) {
            return;
        }

        client.getPacketHandler().write(
                new AssetEditorUpdateModelPreview(path.toPacket(), model.toPacket(), null, CAMERA));
    }
}
