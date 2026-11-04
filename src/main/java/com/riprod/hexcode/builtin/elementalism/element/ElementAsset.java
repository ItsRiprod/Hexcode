package com.riprod.hexcode.builtin.elementalism.element;

import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.hexcode.builtin.elementalism.effects.ElementEffects;
import com.riprod.hexcode.builtin.elementalism.effects.ResolvedEffects;

public abstract class ElementAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ElementAsset>> {
    public static final AssetCodecMapCodec<String, ElementAsset> CODEC;
    public static final BuilderCodec<ElementAsset> ABSTRACT_CODEC;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static AssetStore<String, ElementAsset, DefaultAssetMap<String, ElementAsset>> ASSET_STORE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected String name;
    protected String description;
    protected String lore;
    protected float reclaimEfficiency = 1.0f;
    protected ElementTiers tiers = ElementTiers.EMPTY;
    protected ElementEffects effects = new ElementEffects();
    protected float constructInterval = 0.5f;

    public static AssetStore<String, ElementAsset, DefaultAssetMap<String, ElementAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(ElementAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, ElementAsset> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    public abstract float rate(ElementTickInput input);

    @Override
    public final String getId() {
        return this.id;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nullable
    public String getLore() {
        return this.lore;
    }

    public float getReclaimEfficiency() {
        return this.reclaimEfficiency;
    }

    public ElementTiers getTiers() {
        return this.tiers;
    }

    public ElementEffects getEffects() {
        return this.effects;
    }

    public float getConstructInterval() {
        return this.constructInterval;
    }

    public ResolvedEffects effectsAt(float charge) {
        var reached = this.tiers.tierFor(charge);
        var entries = this.tiers.entries();
        var tier = reached != null ? reached.tier() : entries.isEmpty() ? null : entries.getFirst().tier();
        return ElementEffects.resolve(null, tier != null ? tier.getEffects() : null, this.effects);
    }

    static {
        CODEC = new AssetCodecMapCodec<>("Type", Codec.STRING,
                (a, s) -> a.id = s,
                a -> a.id,
                (a, data) -> a.data = data,
                a -> a.data);
        ABSTRACT_CODEC = BuilderCodec.abstractBuilder(ElementAsset.class)
                .appendInherited(new KeyedCodec<>("Name", Codec.STRING),
                        (a, v) -> a.name = v, a -> a.name,
                        (a, p) -> a.name = p.name)
                .documentation("Translation key for the element name")
                .add()
                .appendInherited(new KeyedCodec<>("Description", Codec.STRING),
                        (a, v) -> a.description = v, a -> a.description,
                        (a, p) -> a.description = p.description)
                .documentation("Translation key for the element description")
                .add()
                .appendInherited(new KeyedCodec<>("Lore", Codec.STRING),
                        (a, v) -> a.lore = v, a -> a.lore,
                        (a, p) -> a.lore = p.lore)
                .documentation("Translation key for the in-world lore of the element")
                .add()
                .<Float>appendInherited(new KeyedCodec<>("ReclaimEfficiency", Codec.FLOAT),
                        (a, v) -> a.reclaimEfficiency = v, a -> a.reclaimEfficiency,
                        (a, p) -> a.reclaimEfficiency = p.reclaimEfficiency)
                .addValidator(Validators.range(0.0f, 1.0f))
                .documentation("Multiplier on charge reclaimed past the last tier reached")
                .add()
                .appendInherited(new KeyedCodec<>("Tiers", ElementTiers.CODEC),
                        (a, v) -> a.tiers = v, a -> a.tiers,
                        (a, p) -> a.tiers = p.tiers)
                .addValidator(ElementTiers.VALIDATOR)
                .documentation("Tiers keyed by the charge threshold at which each is reached")
                .add()
                .appendInherited(new KeyedCodec<>("Effects", ElementEffects.CODEC),
                        (a, v) -> a.effects = v, a -> a.effects,
                        (a, p) -> a.effects = p.effects)
                .documentation("Fallback effects for every tier of the element")
                .add()
                .<Float>appendInherited(new KeyedCodec<>("ConstructInterval", Codec.FLOAT),
                        (a, v) -> a.constructInterval = v, a -> a.constructInterval,
                        (a, p) -> a.constructInterval = p.constructInterval)
                .addValidator(Validators.greaterThan(0.0f))
                .documentation("Seconds between plays of the Construct effect while the element charges")
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ElementAsset::getAssetStore));
    }
}
