package com.riprod.hexcode.core.common.drawing.registry;

import java.util.ArrayList;
import java.util.List;

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

public class TemplateAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, TemplateAsset>> {
    public static final AssetBuilderCodec<String, TemplateAsset> CODEC;
    private static AssetStore<String, TemplateAsset, DefaultAssetMap<String, TemplateAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected String shapeId;
    protected float[] points;
    protected Boolean isTraining;

    public static AssetStore<String, TemplateAsset, DefaultAssetMap<String, TemplateAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(TemplateAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, TemplateAsset> getAssetMap() {
        return (DefaultAssetMap<String, TemplateAsset>) getAssetStore().getAssetMap();
    }

    private TemplateAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getShapeId() {
        return this.shapeId;
    }

    public float[] getPoints() {
        return this.points;
    }

    public Boolean getIsTraining() {
        return this.isTraining;
    }

    public float[][] getPointsAs2D() {
        int count = points.length / 2;
        float[][] result = new float[count][2];
        for (int i = 0; i < count; i++) {
            result[i][0] = points[i * 2];
            result[i][1] = points[i * 2 + 1];
        }
        return result;
    }

    public static List<TemplateAsset> getTemplatesForShape(String shapeId) {
        List<TemplateAsset> result = new ArrayList<>();
        getAssetMap().getAssetMap().forEach((key, asset) -> {
            if (shapeId.equals(asset.getShapeId())) {
                result.add(asset);
            }
        });
        return result;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(TemplateAsset.class, TemplateAsset::new, Codec.STRING, (asset, s) -> {
                    asset.id = s;
                }, (asset) -> {
                    return asset.id;
                }, (asset, data) -> {
                    asset.data = data;
                }, (asset) -> {
                    return asset.data;
                })
                .append(new KeyedCodec<>("ShapeId", Codec.STRING),
                        (a, v) -> a.shapeId = v, a -> a.shapeId)
                .addValidatorLate(() -> ShapeAsset.VALIDATOR_CACHE.getValidator().late())
                .add()
                .append(new KeyedCodec<>("Points", Codec.FLOAT_ARRAY),
                        (a, v) -> a.points = v, a -> a.points)
                .add()
                .append(new KeyedCodec<>("IsTraining", Codec.BOOLEAN),
                        (a, v) -> a.isTraining = v, a -> a.isTraining)
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(TemplateAsset::getAssetStore));
    }
}
