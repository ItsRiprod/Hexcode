package com.riprod.hexcode.core.common.effects.registry;

import java.util.EnumMap;

import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.component.GlyphEffect;

public class GlyphEffects implements JsonAssetWithMap<String, DefaultAssetMap<String, GlyphEffects>> {
    private static final float MIN_VOLUME = 0.0f;
    private static final float MAX_VOLUME = 2.0f;

    public static final AssetBuilderCodec<String, GlyphEffects> CODEC;
    public static final Codec<String> CHILD_ASSET_CODEC;
    private static AssetStore<String, GlyphEffects, DefaultAssetMap<String, GlyphEffects>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;

    protected Color primaryColor;
    protected Color secondaryColor;
    protected Float alpha;
    protected Float volume;
    protected String model;

    protected transient Model resolvedModel;

    protected final EnumMap<EffectSlot, GlyphEffect> slots = new EnumMap<>(EffectSlot.class);

    public static AssetStore<String, GlyphEffects, DefaultAssetMap<String, GlyphEffects>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(GlyphEffects.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, GlyphEffects> getAssetMap() {
        return (DefaultAssetMap<String, GlyphEffects>) getAssetStore().getAssetMap();
    }

    public GlyphEffects() {
    }

    public static GlyphEffects empty() {
        return new GlyphEffects();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Color getPrimaryColor() {
        return this.primaryColor;
    }

    public Color getSecondaryColor() {
        return this.secondaryColor;
    }

    public Float getAlpha() {
        return this.alpha;
    }

    public float getAlphaOrDefault() {
        return this.alpha != null ? this.alpha : 1.0f;
    }

    public Float getVolume() {
        return this.volume;
    }

    public float getVolumeOrDefault() {
        if (this.volume == null) return 1.0f;
        return this.volume < MIN_VOLUME ? MIN_VOLUME : Math.min(this.volume, MAX_VOLUME);
    }

    public String getModel() {
        return this.model;
    }

    public Model getResolvedModel() {
        return this.resolvedModel;
    }

    @Nullable
    public GlyphEffect get(EffectSlot slot) {
        var effect = this.slots.get(slot);
        if (effect == null && (slot == EffectSlot.Entity || slot == EffectSlot.Block)) {
            return this.slots.get(EffectSlot.Hit);
        }
        return effect;
    }

    public void setPrimaryColor(Color color) {
        this.primaryColor = color;
    }

    public void setSecondaryColor(Color color) {
        this.secondaryColor = color;
    }

    public void setAlpha(Float alpha) {
        this.alpha = alpha;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setResolvedModel(Model resolvedModel) {
        this.resolvedModel = resolvedModel;
    }

    public GlyphEffects put(EffectSlot slot, @Nullable GlyphEffect effect) {
        if (effect != null) this.slots.put(slot, effect); else this.slots.remove(slot);
        return this;
    }

    public GlyphEffects palette() {
        var palette = new GlyphEffects();
        palette.primaryColor = this.primaryColor;
        palette.secondaryColor = this.secondaryColor;
        palette.alpha = this.alpha;
        palette.volume = this.volume;
        return palette;
    }

    public GlyphEffects clone() {
        return new GlyphEffects().compose(this);
    }

    public boolean hasBody() {
        return model != null || resolvedModel != null;
    }

    public GlyphEffects compose(@Nullable GlyphEffects other) {
        return compose(other, true);
    }

    public GlyphEffects compose(@Nullable GlyphEffects other, boolean withBody) {
        if (other == null) return this;
        if (other.primaryColor != null) this.primaryColor = other.primaryColor.clone();
        if (other.secondaryColor != null) this.secondaryColor = other.secondaryColor.clone();
        if (other.alpha != null) this.alpha = other.alpha;
        if (other.volume != null) this.volume = other.volume;
        if (withBody && other.model != null) {
            this.model = other.model;
            this.resolvedModel = other.resolvedModel;
        } else if (withBody && other.resolvedModel != null) {
            this.resolvedModel = other.resolvedModel;
        }
        this.slots.putAll(other.slots);
        return this;
    }

    static {
        var builder = AssetBuilderCodec
                .builder(GlyphEffects.class, GlyphEffects::new, Codec.STRING,
                        (asset, s) -> asset.id = s,
                        (asset) -> asset.id,
                        (asset, data) -> asset.data = data,
                        (asset) -> asset.data)
                .appendInherited(new KeyedCodec<>("PrimaryColor", ProtocolCodecs.COLOR),
                        (a, v) -> a.primaryColor = v,
                        a -> a.primaryColor,
                        (a, p) -> {
                            if (p.primaryColor != null) a.primaryColor = p.primaryColor.clone();
                        })
                .add()
                .appendInherited(new KeyedCodec<>("SecondaryColor", ProtocolCodecs.COLOR),
                        (a, v) -> a.secondaryColor = v,
                        a -> a.secondaryColor,
                        (a, p) -> {
                            if (p.secondaryColor != null) a.secondaryColor = p.secondaryColor.clone();
                        })
                .add()
                .<Float>appendInherited(new KeyedCodec<>("Alpha", Codec.FLOAT),
                        (a, v) -> a.alpha = v,
                        a -> a.alpha,
                        (a, p) -> a.alpha = p.alpha)
                .add()
                .<Float>appendInherited(new KeyedCodec<>("Volume", Codec.FLOAT),
                        (a, v) -> a.volume = v,
                        a -> a.volume,
                        (a, p) -> a.volume = p.volume)
                .add()
                .appendInherited(new KeyedCodec<>("Model", Codec.STRING),
                        (a, v) -> a.model = v,
                        a -> a.model,
                        (a, p) -> a.model = p.model)
                .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
                .add();
        for (var slot : EffectSlot.values()) {
            builder.appendInherited(new KeyedCodec<>(slot.name(), GlyphEffect.CODEC),
                    (a, v) -> a.slots.put(slot, v),
                    a -> a.slots.get(slot),
                    (a, p) -> {
                        var inherited = p.slots.get(slot);
                        if (inherited != null) a.slots.put(slot, inherited);
                    })
                    .add();
        }
        CODEC = builder.build();
        CHILD_ASSET_CODEC = new ContainedAssetCodec<>(GlyphEffects.class, CODEC);
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(GlyphEffects::getAssetStore));
    }
}
