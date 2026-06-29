package com.riprod.hexcode.core.common.execution.component;

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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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

    protected HexStats hexStats = new HexStats();
    protected HexStyleAsset style = new HexStyleAsset();
    protected float castDecayRate = 0.0f;

    public abstract Hex getHex(ComponentAccessor<EntityStore> accessor, HexRoot hexRoot);

    public HexStats getHexStats() {
        return this.hexStats;
    }

    public HexStyleAsset getStyle() {
        return this.style;
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
                a -> a.data);

        BASE_CODEC = BuilderCodec.abstractBuilder(HexConfigAsset.class)
                .appendInherited(new KeyedCodec<>("HexStats", HexStats.CODEC),
                        (c, v) -> c.hexStats = v,
                        c -> c.hexStats,
                        (c, p) -> c.hexStats = p.hexStats)
                .add()
                .appendInherited(new KeyedCodec<>("Style", HexStyleAsset.CODEC),
                        (c, v) -> c.style = v,
                        c -> c.style,
                        (c, p) -> c.style = p.style)
                .add()
                .<Float>appendInherited(new KeyedCodec<>("CastDecayRate", Codec.FLOAT),
                        (c, v) -> c.castDecayRate = v,
                        c -> c.castDecayRate,
                        (c, p) -> c.castDecayRate = p.castDecayRate)
                .add()
                .build();

        CHILD_ASSET_CODEC = new ContainedAssetCodec<>(HexConfigAsset.class, CODEC);
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(HexConfigAsset::getAssetStore));
    }
}
