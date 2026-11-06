package com.riprod.hexcode.core.common.imbuement.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;

import javax.annotation.Nullable;

public class EssenceAsset
        implements JsonAssetWithMap<String, DefaultAssetMap<String, EssenceAsset>> {

    public static final AssetBuilderCodec<String, EssenceAsset> CODEC;
    private static AssetStore<String, EssenceAsset, DefaultAssetMap<String, EssenceAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected float volatilityMultiplier = 1.0f;
    @Nullable
    protected String effectsId;

    public static AssetStore<String, EssenceAsset, DefaultAssetMap<String, EssenceAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(EssenceAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, EssenceAsset> getAssetMap() {
        return (DefaultAssetMap<String, EssenceAsset>) getAssetStore().getAssetMap();
    }

    private EssenceAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public float getVolatilityMultiplier() {
        return volatilityMultiplier;
    }

    @Nullable
    public GlyphEffects getEffects() {
        if (this.effectsId == null) return null;
        return GlyphEffects.getAssetMap().getAsset(this.effectsId);
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(EssenceAsset.class, EssenceAsset::new, Codec.STRING,
                        (asset, s) -> asset.id = s,
                        asset -> asset.id,
                        (asset, data) -> asset.data = data,
                        asset -> asset.data)
                .append(new KeyedCodec<>("VolatilityMultiplier", Codec.FLOAT),
                        (a, v) -> a.volatilityMultiplier = v != null ? v : 1.0f,
                        a -> a.volatilityMultiplier)
                .documentation("Volatility multiplier applied to the cast this essence refills. Defaults to 1.0 (no change).")
                .add()
                .append(new KeyedCodec<>("Effects", GlyphEffects.CHILD_ASSET_CODEC),
                        (a, v) -> a.effectsId = v,
                        a -> a.effectsId)
                .documentation("Optional effects composed over the cast this essence refills. Accepts a string id (e.g. \"Essence_Fire\") or an inline block.")
                .addValidatorLate(() -> GlyphEffects.VALIDATOR_CACHE.getValidator().late())
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(EssenceAsset::getAssetStore));
    }
}
