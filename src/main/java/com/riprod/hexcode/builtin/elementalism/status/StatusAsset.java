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
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;

public class StatusAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, StatusAsset>> {
    public static final AssetBuilderCodec<String, StatusAsset> CODEC;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static AssetStore<String, StatusAsset, DefaultAssetMap<String, StatusAsset>> ASSET_STORE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected boolean hidden;
    protected String effectId;
    protected String effectsId;

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

    @Nullable
    public GlyphEffects getEffects() {
        return this.effectsId != null ? GlyphEffects.getAssetMap().getAsset(this.effectsId) : null;
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
                .appendInherited(new KeyedCodec<>("Effects", GlyphEffects.CHILD_ASSET_CODEC),
                        (a, v) -> a.effectsId = v, a -> a.effectsId,
                        (a, p) -> a.effectsId = p.effectsId)
                .addValidatorLate(() -> GlyphEffects.VALIDATOR_CACHE.getValidator().late())
                .documentation("Effects of the status; blocks pulse its Sustain")
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(StatusAsset::getAssetStore));
    }
}
