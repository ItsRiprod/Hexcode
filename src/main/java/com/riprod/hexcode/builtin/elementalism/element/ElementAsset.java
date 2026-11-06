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
import com.riprod.hexcode.builtin.elementalism.element.efficiency.BranchEfficiency;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;

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
    protected String effectsId;
    @Nullable protected BranchEfficiency branchEfficiency;

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

    public float efficiency(ElementTickInput input) {
        return this.branchEfficiency != null ? this.branchEfficiency.efficiency(input) : 1.0f;
    }

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

    @Nullable
    public GlyphEffects getEffects() {
        return this.effectsId != null ? GlyphEffects.getAssetMap().getAsset(this.effectsId) : null;
    }

    @Nullable
    public ElementalTier vesselTier(float charge) {
        var reached = this.tiers.tierFor(charge);
        return reached != null ? reached.tier() : null;
    }

    public GlyphEffects vessel(@Nullable ElementalTier tier) {
        var element = getEffects();
        var vessel = (element != null ? element.clone() : GlyphEffects.empty())
                .compose(tier != null ? tier.getEffects() : null);
        return vessel.put(EffectSlot.Hit, element != null ? element.get(EffectSlot.Hit) : null);
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
                .appendInherited(new KeyedCodec<>("BranchEfficiency", BranchEfficiency.CODEC),
                        (a, v) -> a.branchEfficiency = v, a -> a.branchEfficiency,
                        (a, p) -> a.branchEfficiency = p.branchEfficiency)
                .documentation("Scales the charge rate by the live branches charging this element. Absent leaves the rate unscaled")
                .add()
                .appendInherited(new KeyedCodec<>("Effects", GlyphEffects.CHILD_ASSET_CODEC),
                        (a, v) -> a.effectsId = v, a -> a.effectsId,
                        (a, p) -> a.effectsId = p.effectsId)
                .addValidatorLate(() -> GlyphEffects.VALIDATOR_CACHE.getValidator().late())
                .documentation("Effects the element overrides its host with; Hit is the element striking")
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ElementAsset::getAssetStore));
    }
}
