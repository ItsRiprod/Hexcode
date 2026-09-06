package com.riprod.hexcode.core.common.imbuement.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bson.BsonValue;

import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.codec.HexCacheResource;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.common.imbuement.ImbuementMetadata;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedBlockComponent;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;

public class ImbuementUtils {
    public static Map<String, ImbuementData> readAll(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) return Collections.emptyMap();
        Map<String, ImbuementData> map = item.getFromMetadataOrNull(ImbuementMetadata.KEY, ImbuementMetadata.CODEC);
        return map != null ? map : Collections.emptyMap();
    }

    @Nullable
    public static ImbuementData read(@Nullable ItemStack item, String slotKey) {
        Map<String, ImbuementData> map = readAll(item);
        return map.get(slotKey);
    }

    public static ItemStack write(ItemStack item, String slotKey, @Nullable ImbuementData data) {
        Map<String, ImbuementData> map = new HashMap<>(readAll(item));
        if (data == null) map.remove(slotKey);
        else map.put(slotKey, data);
        return applyMetadata(item, map);
    }

    public static ItemStack clear(ItemStack item, String slotKey) {
        return write(item, slotKey, null);
    }

    public static ItemStack writeAll(ItemStack item, @Nullable Map<String, ImbuementData> slots) {
        return applyMetadata(item, slots);
    }

    @Nullable
    public static ImbuementProfileAsset resolveProfile(@Nullable ItemStack item) {
        return ImbuementProfileUtils.first(item);
    }

    // legacy slotless API — delegates to DEFAULT_SLOT for backwards compat

    @Nullable
    public static ImbuementData read(@Nullable ItemStack item) {
        return read(item, ImbuementMetadata.DEFAULT_SLOT);
    }

    public static ItemStack write(ItemStack item, ImbuementData data) {
        return write(item, ImbuementMetadata.DEFAULT_SLOT, data);
    }

    public static ItemStack clear(ItemStack item) {
        return applyMetadata(item, null);
    }

    private static ItemStack applyMetadata(ItemStack item, @Nullable Map<String, ImbuementData> slots) {
        Map<String, ImbuementData> finalMap = (slots == null || slots.isEmpty()) ? null : new HashMap<>(slots);
        ItemStack out = item.withMetadata(ImbuementMetadata.KEY, ImbuementMetadata.CODEC, finalMap);
        BsonValue blockHolder = (finalMap == null || !isBlockImbuement(item)) ? null : encodeBlockHolder(finalMap);
        return out.withMetadata(ItemStack.Metadata.BLOCK_HOLDER, blockHolder);
    }

    private static boolean isBlockImbuement(ItemStack item) {
        ImbuementProfileAsset profile = resolveProfile(item);
        return profile != null && profile.writesBlockHolder();
    }

    private static BsonValue encodeBlockHolder(Map<String, ImbuementData> slots) {
        Holder<ChunkStore> holder = ChunkStore.REGISTRY.newHolder();
        ImbuedBlockComponent comp = holder.ensureAndGetComponent(ImbuedBlockComponent.getComponentType());
        for (Map.Entry<String, ImbuementData> entry : slots.entrySet()) {
            ImbuementData v = entry.getValue();
            if (v != null) comp.write(entry.getKey(), v.copy());
        }
        return ChunkStore.REGISTRY.getEntityCodec().encode(holder, EmptyExtraInfo.EMPTY);
    }

    // hex resolution / construction helpers

    @Nullable
    public static Hex resolveHex(ImbuementData data, ComponentAccessor<EntityStore> accessor) {
        if (data.getHexCompressedId() != null) {
            HexCacheResource cache = accessor.getResource(HexCacheResource.getResourceType());
            Hex hex = cache != null
                    ? cache.getOrDecode(data.getHexCompressedId())
                    : HexUtils.deserialize(data.getHexCompressedId());
            if (hex != null) return cache != null ? hex : hex.clone();
        }
        if (data.getHex() != null) return data.getHex().clone();
        if (data.getHexAssetId() != null) {
            SavedHexAsset saved = SavedHexAsset.getAssetMap().getAsset(data.getHexAssetId());
            if (saved != null && saved.getHex() != null) return saved.getHex().clone();
        }
        return null;
    }

    public static ImbuementData fromHex(Hex hex) {
        ImbuementData data = new ImbuementData();
        data.setHexFromValue(hex);
        return data;
    }

    public static ImbuementData fromAsset(String hexAssetId) {
        ImbuementData data = new ImbuementData();
        data.setHexCompressedId(hexAssetId);
        return data;
    }
}
