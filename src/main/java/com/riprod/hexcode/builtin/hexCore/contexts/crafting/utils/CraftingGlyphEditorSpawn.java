package com.riprod.hexcode.builtin.hexCore.contexts.crafting.utils;

import java.util.List;

import org.joml.Vector3d;

import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorActivateButtonEvent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotificationType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.GlyphCommitEvent;
import com.riprod.hexcode.api.event.GlyphDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskDispatcher;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.utils.GlyphMath;

public final class CraftingGlyphEditorSpawn {

    public static final String BUTTON_ID = "HexcodeSpawnGlyph";
    public static final String BUTTON_TEXT_ID = "server.hexcode.assetEditor.buttons.spawnGlyph";

    private static final String MESSAGE_PREFIX = "server.hexcode.assetEditor.messages.spawnGlyph.";
    private static final float SPAWN_DISTANCE = 4.0f;
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

        if (store.getComponent(playerRef, CraftingState.getComponentType()) == null) {
            notify(client, AssetEditorPopupNotificationType.Warning, "notCrafting", asset.getId());
            return;
        }

        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(playerRef, store);
        HexcodeSessionComponent session = pedestal != null ? SessionUtils.resolveSession(pedestal, store) : null;
        HeadRotation head = store.getComponent(playerRef, HeadRotation.getComponentType());
        if (pedestal == null || session == null || head == null) {
            notify(client, AssetEditorPopupNotificationType.Error, "noSession", asset.getId());
            return;
        }

        Vector3d eyePos = PlayerUtils.getPlayerEyePosition(store, playerRef);
        Vector3d spawnPos = GlyphMath.sphericalToCartesian(eyePos, head.getRotation().y,
                head.getRotation().x, SPAWN_DISTANCE);

        Vector3d anchorPos = PedestalEntity.getAnchorPosition(session.getPedestalLocation());
        double maxRadius = pedestal.getMaxRadius();
        if (spawnPos.distanceSquared(anchorPos) > maxRadius * maxRadius) {
            notify(client, AssetEditorPopupNotificationType.Warning, "outOfRange", asset.getId());
            return;
        }

        Glyph glyph = new Glyph(asset, SPAWN_QUALITY, SPAWN_QUALITY);
        GlyphCommitEvent commit = new GlyphCommitEvent(playerRef, glyph, asset, CraftingState.CONTEXT_ID);
        store.invoke(playerRef, commit);
        if (commit.isCancelled()) {
            notify(client, AssetEditorPopupNotificationType.Warning, "cancelled", asset.getId());
            return;
        }

        ObeliskDispatcher.dispatchGlyphDrawn(store, pedestal, playerRef, glyph);
        HytaleServer.get().getEventBus().dispatchFor(GlyphDrawnEvent.class)
                .dispatch(new GlyphDrawnEvent(playerRef, glyph, List.of(), asset));

        CraftingGlyphSpawner.spawnDrawnGlyph(store, glyph, session, spawnPos, head.getRotation(), playerRef);
        notify(client, AssetEditorPopupNotificationType.Success, "spawned", asset.getId());
    }

    private static void notify(EditorClient client, AssetEditorPopupNotificationType type, String key,
            String glyphId) {
        client.sendPopupNotification(type, Message.translation(MESSAGE_PREFIX + key).param("id", glyphId));
    }
}
