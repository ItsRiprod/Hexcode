package com.riprod.hexcode.core.common.execution.component;

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

    public static final AssetCodecMapCodec<String, HexConfigAsset> CODEC;
    public static final Codec<String> CHILD_ASSET_CODEC;
    public static final BuilderCodec<HexConfigAsset> BASE_CODEC;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static AssetStore<String, HexConfigAsset, DefaultAssetMap<String, HexConfigAsset>> ASSET_STORE;

    protected AssetExtraInfo.Data data;
    protected String id;

    protected HexStats hexStats = new HexStats();
    protected String styleId;
    protected float tierScale = 1.0f;
    protected boolean requireMagicCharges = true;
    protected boolean consumeMana = true;
    protected boolean applyVolatilityDecay = true;
    protected boolean bypassVolatilityDepletion = false;

    public abstract Hex getHex(ComponentAccessor<EntityStore> accessor, HexRoot hexRoot);

    public HexStats getHexStats() {
        return this.hexStats;
    }

    public HexStyleAsset getStyle() {
        if (this.styleId == null) return null;
        return HexStyleAsset.getAssetMap().getAsset(this.styleId);
    }

    public float getTierScale() {
        return this.tierScale;
    }

    public boolean isRequireMagicCharges() {
        return this.requireMagicCharges;
    }

    public boolean isConsumeMana() {
        return this.consumeMana;
    }

    public boolean isApplyVolatilityDecay() {
        return this.applyVolatilityDecay;
    }

    public boolean isBypassVolatilityDepletion() {
        return this.bypassVolatilityDepletion;
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
                a -> a.data);

        BASE_CODEC = BuilderCodec.abstractBuilder(HexConfigAsset.class)
                .appendInherited(new KeyedCodec<>("HexStats", HexStats.CODEC),
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
                .<Float>appendInherited(new KeyedCodec<>("TierScale", Codec.FLOAT),
                        (c, v) -> c.tierScale = v,
                        c -> c.tierScale,
                        (c, p) -> c.tierScale = p.tierScale)
                .add()
                .<Boolean>appendInherited(new KeyedCodec<>("RequireMagicCharges", Codec.BOOLEAN),
                        (c, v) -> c.requireMagicCharges = v,
                        c -> c.requireMagicCharges,
                        (c, p) -> c.requireMagicCharges = p.requireMagicCharges)
                .add()
                .<Boolean>appendInherited(new KeyedCodec<>("ConsumeMana", Codec.BOOLEAN),
                        (c, v) -> c.consumeMana = v,
                        c -> c.consumeMana,
                        (c, p) -> c.consumeMana = p.consumeMana)
                .add()
                .<Boolean>appendInherited(new KeyedCodec<>("ApplyVolatilityDecay", Codec.BOOLEAN),
                        (c, v) -> c.applyVolatilityDecay = v,
                        c -> c.applyVolatilityDecay,
                        (c, p) -> c.applyVolatilityDecay = p.applyVolatilityDecay)
                .add()
                .<Boolean>appendInherited(new KeyedCodec<>("BypassVolatilityDepletion", Codec.BOOLEAN),
                        (c, v) -> c.bypassVolatilityDepletion = v,
                        c -> c.bypassVolatilityDepletion,
                        (c, p) -> c.bypassVolatilityDepletion = p.bypassVolatilityDepletion)
                .add()
                .build();

        CHILD_ASSET_CODEC = new ContainedAssetCodec<>(HexConfigAsset.class, CODEC);
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(HexConfigAsset::getAssetStore));
    }
}
