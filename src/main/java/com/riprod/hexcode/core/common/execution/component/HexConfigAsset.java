package com.riprod.hexcode.core.common.execution.component;

import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public abstract class HexConfigAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, HexConfigAsset>> {

    public static final float DEFAULT_TIER_SCALE = 1.0f;
    public static final boolean DEFAULT_REQUIRE_MAGIC_CHARGES = true;
    public static final boolean DEFAULT_CONSUME_MANA = true;
    public static final boolean DEFAULT_APPLY_VOLATILITY_DECAY = true;
    public static final boolean DEFAULT_BYPASS_VOLATILITY_DEPLETION = false;

    public static final AssetCodecMapCodec<String, HexConfigAsset> CODEC;
    public static final Codec<String> CHILD_ASSET_CODEC;
    public static final BuilderCodec<HexConfigAsset> BASE_CODEC;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static AssetStore<String, HexConfigAsset, DefaultAssetMap<String, HexConfigAsset>> ASSET_STORE;

    protected AssetExtraInfo.Data data;
    protected String id;

    @Nullable protected HexStatsDefaults hexStats;
    @Nullable protected String styleId;
    @Nullable protected Float manaCost;
    @Nullable protected Float manaMultiplier;
    @Nullable protected Float tierScale;
    @Nullable protected Boolean requireMagicCharges;
    @Nullable protected Boolean consumeMana;
    @Nullable protected Boolean applyVolatilityDecay;
    @Nullable protected Boolean bypassVolatilityDepletion;

    @Nullable
    public Hex getHex(ComponentAccessor<EntityStore> accessor, HexRoot hexRoot) {
        return null;
    }

    @Nullable
    public HexStyleAsset getStyle() {
        if (this.styleId == null) return null;
        return HexStyleAsset.getAssetMap().getAsset(this.styleId);
    }

    public float resolveTierScale() {
        return this.tierScale != null ? this.tierScale : DEFAULT_TIER_SCALE;
    }

    public void applyTo(HexContext context) {
        if (context == null) return;

        if (manaCost != null) context.setManaCost(manaCost);
        if (manaMultiplier != null) context.setManaMultiplier(context.getManaMultiplier() * manaMultiplier);

        HexStyleAsset style = getStyle();
        if (style != null) context.setStyle(style.clone());

        if (tierScale != null) context.setTierScale(tierScale);
        if (requireMagicCharges != null) context.setRequireMagicCharges(requireMagicCharges);
        if (consumeMana != null) context.setConsumeMana(consumeMana);
        if (applyVolatilityDecay != null) context.setApplyVolatilityDecay(applyVolatilityDecay);
        if (bypassVolatilityDepletion != null) context.setBypassVolatilityDepletion(bypassVolatilityDepletion);

        if (hexStats != null) hexStats.applyTo(context.cast());
    }

    public static AssetStore<String, HexConfigAsset, DefaultAssetMap<String, HexConfigAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(HexConfigAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, HexConfigAsset> getAssetMap() {
        return (DefaultAssetMap<String, HexConfigAsset>) getAssetStore().getAssetMap();
    }

    @Override
    public String getId() {
        return this.id;
    }

    static {
        CODEC = new AssetCodecMapCodec<>("HexStorageType", Codec.STRING,
                (a, s) -> a.id = s,
                a -> a.id,
                (a, d) -> a.data = d,
                a -> a.data,
                true);

        BASE_CODEC = BuilderCodec.abstractBuilder(HexConfigAsset.class)
                .appendInherited(new KeyedCodec<>("HexStats", HexStatsDefaults.CODEC),
                        (c, v) -> c.hexStats = v,
                        c -> c.hexStats,
                        (c, p) -> c.hexStats = p.hexStats)
                .add()
                .appendInherited(new KeyedCodec<>("Style", HexStyleAsset.CHILD_ASSET_CODEC),
                        (c, v) -> c.styleId = v,
                        c -> c.styleId,
                        (c, p) -> c.styleId = p.styleId)
                .addValidatorLate(() -> HexStyleAsset.VALIDATOR_CACHE.getValidator().late())
                .add()
                .appendInherited(new KeyedCodec<>("ManaCost", Codec.FLOAT),
                        (c, v) -> c.manaCost = v,
                        c -> c.manaCost,
                        (c, p) -> c.manaCost = p.manaCost)
                .documentation("Replaces the mana cost computed from the hex.")
                .add()
                .appendInherited(new KeyedCodec<>("ManaMultiplier", Codec.FLOAT),
                        (c, v) -> c.manaMultiplier = v,
                        c -> c.manaMultiplier,
                        (c, p) -> c.manaMultiplier = p.manaMultiplier)
                .documentation("Scales the mana cost. Compounds with earlier layers.")
                .add()
                .appendInherited(new KeyedCodec<>("TierScale", Codec.FLOAT),
                        (c, v) -> c.tierScale = v,
                        c -> c.tierScale,
                        (c, p) -> c.tierScale = p.tierScale)
                .add()
                .appendInherited(new KeyedCodec<>("RequireMagicCharges", Codec.BOOLEAN),
                        (c, v) -> c.requireMagicCharges = v,
                        c -> c.requireMagicCharges,
                        (c, p) -> c.requireMagicCharges = p.requireMagicCharges)
                .add()
                .appendInherited(new KeyedCodec<>("ConsumeMana", Codec.BOOLEAN),
                        (c, v) -> c.consumeMana = v,
                        c -> c.consumeMana,
                        (c, p) -> c.consumeMana = p.consumeMana)
                .add()
                .appendInherited(new KeyedCodec<>("ApplyVolatilityDecay", Codec.BOOLEAN),
                        (c, v) -> c.applyVolatilityDecay = v,
                        c -> c.applyVolatilityDecay,
                        (c, p) -> c.applyVolatilityDecay = p.applyVolatilityDecay)
                .add()
                .appendInherited(new KeyedCodec<>("BypassVolatilityDepletion", Codec.BOOLEAN),
                        (c, v) -> c.bypassVolatilityDepletion = v,
                        c -> c.bypassVolatilityDepletion,
                        (c, p) -> c.bypassVolatilityDepletion = p.bypassVolatilityDepletion)
                .add()
                .build();

        CHILD_ASSET_CODEC = new ContainedAssetCodec<>(HexConfigAsset.class, CODEC);
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(HexConfigAsset::getAssetStore));
    }
}
