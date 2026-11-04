package com.riprod.hexcode.builtin.elementalism.status;

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
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.builtin.elementalism.effects.ElementEffects;

public class StatusAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, StatusAsset>> {
    public static final AssetBuilderCodec<String, StatusAsset> CODEC;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static AssetStore<String, StatusAsset, DefaultAssetMap<String, StatusAsset>> ASSET_STORE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected boolean hidden;
    protected String effectId;
    protected ElementEffects effects = new ElementEffects();
    protected float auraInterval = 1.0f;

    public static AssetStore<String, StatusAsset, DefaultAssetMap<String, StatusAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(StatusAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, StatusAsset> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    protected StatusAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    @Nullable
    public String getEffectId() {
        return this.effectId;
    }

    public ElementEffects getEffects() {
        return this.effects;
    }

    public float getAuraInterval() {
        return this.auraInterval;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(StatusAsset.class, StatusAsset::new, Codec.STRING,
                        (a, s) -> a.id = s,
                        a -> a.id,
                        (a, data) -> a.data = data,
                        a -> a.data)
                .appendInherited(new KeyedCodec<>("Hidden", Codec.BOOLEAN),
                        (a, v) -> a.hidden = v, a -> a.hidden,
                        (a, p) -> a.hidden = p.hidden)
                .documentation("Hidden statuses are only applied by composites, never by a tier")
                .add()
                .appendInherited(new KeyedCodec<>("EffectId", Codec.STRING),
                        (a, v) -> a.effectId = v, a -> a.effectId,
                        (a, p) -> a.effectId = p.effectId)
                .addValidator(EntityEffect.VALIDATOR_CACHE.getValidator())
                .documentation("Entity effect applied while the status is on an entity")
                .add()
                .appendInherited(new KeyedCodec<>("Effects", ElementEffects.CODEC),
                        (a, v) -> a.effects = v, a -> a.effects,
                        (a, p) -> a.effects = p.effects)
                .documentation("Particles, sounds and colors played for the status")
                .add()
                .<Float>appendInherited(new KeyedCodec<>("AuraInterval", Codec.FLOAT),
                        (a, v) -> a.auraInterval = v, a -> a.auraInterval,
                        (a, p) -> a.auraInterval = p.auraInterval)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Seconds between plays of the Aura effect while the status is applied")
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(StatusAsset::getAssetStore));
    }
}
