package com.riprod.hexcode.core.common.drawing.registry;

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
import com.hypixel.hytale.codec.validation.Validators;

public class ShapeAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ShapeAsset>> {
    public static final AssetBuilderCodec<String, ShapeAsset> CODEC;
    private static AssetStore<String, ShapeAsset, DefaultAssetMap<String, ShapeAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected Boolean canRotate;
    protected Boolean centerFilled;
    protected long expectedSpeed;

    public static AssetStore<String, ShapeAsset, DefaultAssetMap<String, ShapeAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(ShapeAsset.class);
        }

        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, ShapeAsset> getAssetMap() {
        return (DefaultAssetMap<String, ShapeAsset>) getAssetStore().getAssetMap();
    }

    private ShapeAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Boolean getCanRotate() {
        return this.canRotate;
    }

    public Boolean getCenterFilled() {
        return this.centerFilled;
    }

    public long getExpectedSpeed() {
        return this.expectedSpeed;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(ShapeAsset.class, ShapeAsset::new, Codec.STRING, (GlyphShapeAsset, s) -> {
                    GlyphShapeAsset.id = s;
                }, (GlyphShapeAsset) -> {
                    return GlyphShapeAsset.id;
                }, (asset, data) -> {
                    asset.data = data;
                }, (asset) -> {
                    return asset.data;
                })
                .append(new KeyedCodec<>("CanRotate", Codec.BOOLEAN),
                        (a, v) -> a.canRotate = v, a -> a.canRotate)
                .add()
                .append(new KeyedCodec<>("CenterFilled", Codec.BOOLEAN),
                        (a, v) -> a.centerFilled = v, a -> a.centerFilled)
                .add()
                .append(new KeyedCodec<>("ExpectedSpeed", Codec.LONG),
                        (a, v) -> a.expectedSpeed = v, a -> a.expectedSpeed)
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ShapeAsset::getAssetStore));
    }
}
