package com.riprod.hexcode.builtin.statly.assets;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;

public class ElementAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ElementAsset>> {
    public static final AssetBuilderCodec<String, ElementAsset> CODEC;
    private static AssetStore<String, ElementAsset, DefaultAssetMap<String, ElementAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected LinkedHashMap<String, BaseElementInteraction> interactions = new LinkedHashMap<>();

    public static AssetStore<String, ElementAsset, DefaultAssetMap<String, ElementAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(ElementAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, ElementAsset> getAssetMap() {
        return (DefaultAssetMap<String, ElementAsset>) getAssetStore().getAssetMap();
    }

    private ElementAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Map<String, BaseElementInteraction> getInteractions() {
        return Collections.unmodifiableMap(this.interactions);
    }

    @Nullable
    public BaseElementInteraction getInteraction(String attackerCauseId) {
        return this.interactions.get(attackerCauseId);
    }

    static {
        CODEC = buildCodec();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ElementAsset::getAssetStore));
    }

    @SuppressWarnings("unchecked")
    private static AssetBuilderCodec<String, ElementAsset> buildCodec() {
        Codec<Map<String, BaseElementInteraction>> interactionMapCodec = (Codec<Map<String, BaseElementInteraction>>) (Codec<?>) new MapCodec<>(
                BaseElementInteraction.CODEC, LinkedHashMap::new, false);
        return AssetBuilderCodec
                .builder(ElementAsset.class, ElementAsset::new, Codec.STRING,
                        (asset, s) -> asset.id = s,
                        asset -> asset.id,
                        (asset, data) -> asset.data = data,
                        asset -> asset.data)
                .appendInherited(new KeyedCodec<>("Interactions", interactionMapCodec),
                        (a, v) -> a.interactions = v != null ? new LinkedHashMap<>(v) : new LinkedHashMap<>(),
                        a -> a.interactions,
                        (a, p) -> a.interactions = new LinkedHashMap<>(p.interactions))
                .addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator())
                .documentation("Reactions keyed by incoming DamageCause; the defender's status drives the interaction")
                .add()
                .build();
    }
}
