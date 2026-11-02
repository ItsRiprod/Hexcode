package com.riprod.hexcode.core.common.execution.config;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.component.CastHexComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastStyleComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastHolder;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public abstract class HexConfigAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, HexConfigAsset>> {

    public static final AssetCodecMapCodec<String, HexConfigAsset> CODEC;
    public static final Codec<String> CHILD_ASSET_CODEC;
    public static final BuilderCodec<HexConfigAsset> BASE_CODEC;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static AssetStore<String, HexConfigAsset, DefaultAssetMap<String, HexConfigAsset>> ASSET_STORE;

    protected AssetExtraInfo.Data data;
    protected String id;

    @Nullable protected Map<String, CastOverlay<?>> hexStats;
    @Nullable protected String styleId;

    @Nullable
    protected Hex resolveHex(@Nonnull Ref<EntityStore> root, @Nonnull ComponentAccessor<EntityStore> accessor) {
        return null;
    }

    @Nullable
    public HexStyleAsset getStyle() {
        if (this.styleId == null) return null;
        return HexStyleAsset.getAssetMap().getAsset(this.styleId);
    }

    public void queueOnto(@Nonnull CastHolder holder, @Nonnull ComponentAccessor<EntityStore> accessor) {
        var hex = resolveHex(holder.getRoot(), accessor);
        if (hex != null) holder.put(CastHexComponent.TYPE, new CastHexComponent(hex));

        if (styleId != null) holder.overlay(CastStyleComponent.TYPE, styleOverlay());

        if (hexStats == null || hexStats.isEmpty()) return;
        for (var entry : hexStats.entrySet()) {
            var type = HexComponentRegistry.getType(entry.getKey());
            if (type != null) queue(holder, type, entry.getValue());
        }
    }

    private CastOverlay<CastStyleComponent> styleOverlay() {
        return target -> {
            var style = getStyle();
            if (style != null) target.set(style.clone());
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends CastComponent> void queue(CastHolder holder, CastComponentType<T> type,
            CastOverlay<?> overlay) {
        holder.overlay(type, (CastOverlay<T>) overlay);
    }

    public static AssetStore<String, HexConfigAsset, DefaultAssetMap<String, HexConfigAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(HexConfigAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, HexConfigAsset> getAssetMap() {
        return (DefaultAssetMap<String, HexConfigAsset>) getAssetStore().getAssetMap();
    }

    @Override
    public String getId() {
        return this.id;
    }

    static {
        CODEC = new AssetCodecMapCodec<>("HexStorageType", Codec.STRING,
                (a, s) -> a.id = s,
                a -> a.id,
                (a, d) -> a.data = d,
                a -> a.data,
                true);

        BASE_CODEC = BuilderCodec.abstractBuilder(HexConfigAsset.class)
                .appendInherited(new KeyedCodec<>("HexStats", new CastOverlayMapCodec()),
                        (c, v) -> c.hexStats = v,
                        c -> c.hexStats,
                        (c, p) -> c.hexStats = p.hexStats)
                .add()
                .appendInherited(new KeyedCodec<>("Style", HexStyleAsset.CHILD_ASSET_CODEC),
                        (c, v) -> c.styleId = v,
                        c -> c.styleId,
                        (c, p) -> c.styleId = p.styleId)
                .addValidatorLate(() -> HexStyleAsset.VALIDATOR_CACHE.getValidator().late())
                .add()
                .build();

        CHILD_ASSET_CODEC = new ContainedAssetCodec<>(HexConfigAsset.class, CODEC);
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(HexConfigAsset::getAssetStore));
    }
}
