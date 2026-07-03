package com.riprod.hexcode.builtin.hextreme.execution.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;
import com.riprod.hexcode.utils.HexSlot;

public class PageConfig extends HexConfigAsset {

    public static final String METADATA_KEY = "PageHex";

    public Hex getHex(ComponentAccessor<EntityStore> accessor, HexRoot hexRoot) {
        Ref<EntityStore> playerRef = hexRoot.getSourceRef(accessor);
        if (playerRef == null || !playerRef.isValid()) {
            return null;
        }
        return resolvePageHex(PlayerUtils.getHandItem(accessor, playerRef, HexSlot.MainHand));
    }

    public static Hex resolvePageHex(ItemStack page) {
        if (page == null || page.isEmpty()) {
            return null;
        }
        String hexId = page.getFromMetadataOrNull(METADATA_KEY, Codec.STRING);
        if (hexId == null) {
            return null;
        }
        SavedHexAsset saved = SavedHexAsset.getAssetMap().getAsset(hexId);
        return saved != null && saved.getHex() != null ? saved.getHex().clone() : null;
    }

    public static final BuilderCodec<PageConfig> CODEC = BuilderCodec
            .builder(PageConfig.class, PageConfig::new, PageConfig.BASE_CODEC)
            .build();
}
