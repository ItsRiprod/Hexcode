package com.riprod.hexcode.core.common.appearance;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HexAppearanceComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, HexAppearanceComponent> componentType;

    private static final int VERSION_FLAT = 1;

    public static final BuilderCodec<HexAppearanceComponent> CODEC = BuilderCodec
            .builder(HexAppearanceComponent.class, HexAppearanceComponent::new)
            .versioned()
            .codecVersion(VERSION_FLAT)
            .append(new KeyedCodec<>("OriginalModel", Model.ModelReference.CODEC),
                    (c, v) -> {},
                    c -> null)
            .setVersionRange(0, 0)
            .add()
            .append(new KeyedCodec<>("OriginalModel", Codec.STRING),
                    (c, v) -> c.originalModelAssetId = v,
                    c -> c.originalModelAssetId)
            .setVersionRange(VERSION_FLAT, BuilderCodec.UNSET_MAX_VERSION)
            .add()
            .append(new KeyedCodec<>("OriginalScale", Codec.FLOAT),
                    (c, v) -> c.originalScale = v,
                    c -> c.originalScale)
            .add()
            .append(new KeyedCodec<>("OriginalStatic", Codec.BOOLEAN, true),
                    (c, v) -> c.originalStatic = v,
                    c -> c.originalStatic)
            .add()
            .append(new KeyedCodec<>("OriginalRandomAttachments", MapCodec.STRING_HASH_MAP_CODEC, true),
                    (c, v) -> c.originalRandomAttachmentIds = v,
                    c -> c.originalRandomAttachmentIds)
            .add()
            .append(new KeyedCodec<>("OriginalNameplate", Codec.STRING, true),
                    (c, v) -> c.originalNameplate = v,
                    c -> c.originalNameplate)
            .add()
            .build();

    public static void setComponentType(ComponentType<EntityStore, HexAppearanceComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexAppearanceComponent> getComponentType() {
        return componentType;
    }

    @Nullable
    private String originalModelAssetId;

    private float originalScale = 1.0f;

    private boolean originalStatic;

    @Nullable
    private Map<String, String> originalRandomAttachmentIds;

    @Nullable
    private String originalNameplate;

    @Nullable
    private transient PlayerSkin originalSkin;

    private final transient Map<String, AppearanceLayer> layers = new LinkedHashMap<>();

    public HexAppearanceComponent() {
    }

    public HexAppearanceComponent(@Nullable String originalModelAssetId, float originalScale,
            boolean originalStatic, @Nullable Map<String, String> originalRandomAttachmentIds) {
        this.originalModelAssetId = originalModelAssetId;
        this.originalScale = originalScale > 0f ? originalScale : 1.0f;
        this.originalStatic = originalStatic;
        this.originalRandomAttachmentIds = originalRandomAttachmentIds;
    }

    @Nullable
    public String getOriginalModelAssetId() {
        return originalModelAssetId;
    }

    public float getOriginalScale() {
        return originalScale;
    }

    public boolean isOriginalStatic() {
        return originalStatic;
    }

    @Nullable
    public Map<String, String> getOriginalRandomAttachmentIds() {
        return originalRandomAttachmentIds;
    }

    @Nullable
    public PlayerSkin getOriginalSkin() {
        return originalSkin;
    }

    public void setOriginalSkin(@Nullable PlayerSkin originalSkin) {
        this.originalSkin = originalSkin;
    }

    @Nullable
    public String getOriginalNameplate() {
        return originalNameplate;
    }

    public void setOriginalNameplate(@Nullable String originalNameplate) {
        this.originalNameplate = originalNameplate;
    }

    public void putLayer(String layerId, AppearanceLayer layer) {
        layers.put(layerId, layer);
    }

    public void removeLayer(String layerId) {
        layers.remove(layerId);
    }

    public void clearLayers() {
        layers.clear();
    }

    public boolean hasLayers() {
        return !layers.isEmpty();
    }

    public Map<String, AppearanceLayer> getLayers() {
        return layers;
    }

    @Nonnull
    @Override
    public HexAppearanceComponent clone() {
        HexAppearanceComponent copy = new HexAppearanceComponent(
                originalModelAssetId, originalScale, originalStatic, originalRandomAttachmentIds);
        copy.originalNameplate = originalNameplate;
        copy.originalSkin = originalSkin;
        copy.layers.putAll(layers);
        return copy;
    }
}
