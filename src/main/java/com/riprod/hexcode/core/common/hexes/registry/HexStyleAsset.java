package com.riprod.hexcode.core.common.hexes.registry;

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
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;

public class HexStyleAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, HexStyleAsset>> {
    public static final float MIN_VOLUME = 0.0f;
    public static final float MAX_VOLUME = 2.0f;

    public static final AssetBuilderCodec<String, HexStyleAsset> CODEC;
    public static final Codec<String> CHILD_ASSET_CODEC;
    private static AssetStore<String, HexStyleAsset, DefaultAssetMap<String, HexStyleAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;

    // overridable
    protected Color primaryColor;
    protected Color secondaryColor;
    protected Float alpha;
    protected Float scale;
    protected Float volume;
    protected ModelParticle styleParticle;
    protected String primaryModel;

    // locked (essence of the consumer)
    protected ModelParticle primaryParticle;
    protected ModelParticle secondaryParticle;
    protected ModelParticle tertiaryParticle;
    protected String primarySound;
    protected String secondarySound;
    protected String tertiarySound;

    public static AssetStore<String, HexStyleAsset, DefaultAssetMap<String, HexStyleAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(HexStyleAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, HexStyleAsset> getAssetMap() {
        return (DefaultAssetMap<String, HexStyleAsset>) getAssetStore().getAssetMap();
    }

    public HexStyleAsset() {
    }

    public static HexStyleAsset empty() {
        return new HexStyleAsset();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Color getPrimaryColor() {
        return this.primaryColor;
    }

    public ColorLight getPrimaryLight() {
        if (this.primaryColor == null) return null;
        return new ColorLight( (byte) 8, this.primaryColor.red, this.primaryColor.green,
            this.primaryColor.blue);
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

    public Float getScale() {
        return this.scale;
    }

    public float getScaleOrDefault() {
        return this.scale != null ? this.scale : 1.0f;
    }

    public Float getVolume() {
        return this.volume;
    }

    public float getVolumeOrDefault() {
        if (this.volume == null) return 1.0f;
        return this.volume < MIN_VOLUME ? MIN_VOLUME : Math.min(this.volume, MAX_VOLUME);
    }

    public ModelParticle getStyleParticle() {
        return this.styleParticle;
    }

    public ModelParticle getPrimaryParticle() {
        return this.primaryParticle;
    }

    public ModelParticle getSecondaryParticle() {
        return this.secondaryParticle;
    }

    public ModelParticle getTertiaryParticle() {
        return this.tertiaryParticle;
    }

    public String getPrimarySound() {
        return this.primarySound;
    }

    public String getSecondarySound() {
        return this.secondarySound;
    }

    public String getTertiarySound() {
        return this.tertiarySound;
    }

    public String getPrimaryModel() {
        return this.primaryModel;
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

    public void setScale(Float scale) {
        this.scale = scale;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public void setStyleParticle(ModelParticle particle) {
        this.styleParticle = particle;
    }

    public void setPrimaryModel(String primaryModel) {
        this.primaryModel = primaryModel;
    }

    public HexStyleAsset clone() {
        HexStyleAsset copy = new HexStyleAsset();
        copy.primaryColor = this.primaryColor != null ? this.primaryColor.clone() : null;
        copy.secondaryColor = this.secondaryColor != null ? this.secondaryColor.clone() : null;
        copy.alpha = this.alpha;
        copy.scale = this.scale;
        copy.volume = this.volume;
        copy.styleParticle = this.styleParticle;
        copy.primaryParticle = this.primaryParticle;
        copy.secondaryParticle = this.secondaryParticle;
        copy.tertiaryParticle = this.tertiaryParticle;
        copy.primarySound = this.primarySound;
        copy.secondarySound = this.secondarySound;
        copy.tertiarySound = this.tertiarySound;
        copy.primaryModel = this.primaryModel;
        return copy;
    }

    // layers all non-null fields from other onto this (used at cast-start staff+book composition)
    public HexStyleAsset compose(HexStyleAsset other) {
        if (other == null) return this;
        if (other.primaryColor != null) this.primaryColor = other.primaryColor.clone();
        if (other.secondaryColor != null) this.secondaryColor = other.secondaryColor.clone();
        if (other.alpha != null) this.alpha = other.alpha;
        if (other.scale != null) this.scale = other.scale;
        if (other.volume != null) this.volume = other.volume;
        if (other.styleParticle != null) this.styleParticle = other.styleParticle;
        if (other.primaryParticle != null) this.primaryParticle = other.primaryParticle;
        if (other.secondaryParticle != null) this.secondaryParticle = other.secondaryParticle;
        if (other.tertiaryParticle != null) this.tertiaryParticle = other.tertiaryParticle;
        if (other.primarySound != null) this.primarySound = other.primarySound;
        if (other.secondarySound != null) this.secondarySound = other.secondarySound;
        if (other.tertiarySound != null) this.tertiarySound = other.tertiarySound;
        if (other.primaryModel != null) this.primaryModel = other.primaryModel;
        return this;
    }

    // layers only overridable fields from other onto this (used by StyleGlyph runtime override)
    public HexStyleAsset applyOverride(HexStyleAsset other) {
        if (other == null) return this;
        if (other.primaryColor != null) this.primaryColor = other.primaryColor.clone();
        if (other.secondaryColor != null) this.secondaryColor = other.secondaryColor.clone();
        if (other.alpha != null) this.alpha = other.alpha;
        if (other.scale != null) this.scale = other.scale;
        if (other.volume != null) this.volume = other.volume;
        if (other.styleParticle != null) this.styleParticle = other.styleParticle;
        if (other.primaryModel != null) this.primaryModel = other.primaryModel;
        return this;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(HexStyleAsset.class, HexStyleAsset::new, Codec.STRING,
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
                .<Float>appendInherited(new KeyedCodec<>("Scale", Codec.FLOAT),
                        (a, v) -> a.scale = v,
                        a -> a.scale,
                        (a, p) -> a.scale = p.scale)
                .add()
                .<Float>appendInherited(new KeyedCodec<>("Volume", Codec.FLOAT),
                        (a, v) -> a.volume = v,
                        a -> a.volume,
                        (a, p) -> a.volume = p.volume)
                .add()
                .appendInherited(new KeyedCodec<>("StyleParticle", ModelParticle.CODEC),
                        (a, v) -> a.styleParticle = v,
                        a -> a.styleParticle,
                        (a, p) -> a.styleParticle = p.styleParticle)
                .add()
                .appendInherited(new KeyedCodec<>("PrimaryParticle", ModelParticle.CODEC),
                        (a, v) -> a.primaryParticle = v,
                        a -> a.primaryParticle,
                        (a, p) -> a.primaryParticle = p.primaryParticle)
                .add()
                .appendInherited(new KeyedCodec<>("SecondaryParticle", ModelParticle.CODEC),
                        (a, v) -> a.secondaryParticle = v,
                        a -> a.secondaryParticle,
                        (a, p) -> a.secondaryParticle = p.secondaryParticle)
                .add()
                .appendInherited(new KeyedCodec<>("TertiaryParticle", ModelParticle.CODEC),
                        (a, v) -> a.tertiaryParticle = v,
                        a -> a.tertiaryParticle,
                        (a, p) -> a.tertiaryParticle = p.tertiaryParticle)
                .add()
                .appendInherited(new KeyedCodec<>("PrimarySound", Codec.STRING),
                        (a, v) -> a.primarySound = v,
                        a -> a.primarySound,
                        (a, p) -> a.primarySound = p.primarySound)
                .addValidatorLate(() -> SoundEvent.VALIDATOR_CACHE.getValidator().late())
                .add()
                .appendInherited(new KeyedCodec<>("SecondarySound", Codec.STRING),
                        (a, v) -> a.secondarySound = v,
                        a -> a.secondarySound,
                        (a, p) -> a.secondarySound = p.secondarySound)
                .addValidatorLate(() -> SoundEvent.VALIDATOR_CACHE.getValidator().late())
                .add()
                .appendInherited(new KeyedCodec<>("TertiarySound", Codec.STRING),
                        (a, v) -> a.tertiarySound = v,
                        a -> a.tertiarySound,
                        (a, p) -> a.tertiarySound = p.tertiarySound)
                .addValidatorLate(() -> SoundEvent.VALIDATOR_CACHE.getValidator().late())
                .add()
                .appendInherited(new KeyedCodec<>("PrimaryModel", Codec.STRING),
                        (a, v) -> a.primaryModel = v,
                        a -> a.primaryModel,
                        (a, p) -> a.primaryModel = p.primaryModel)
                .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
                .add()
                .build();
        CHILD_ASSET_CODEC = new ContainedAssetCodec<>(HexStyleAsset.class, CODEC);
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(HexStyleAsset::getAssetStore));
    }
}
