package com.riprod.hexcode.core.common.hexbook.component;

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
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.riprod.hexcode.core.common.hexes.component.HexColors;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

import javax.annotation.Nullable;

public class HexBookAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, HexBookAsset>> {
    public static final AssetBuilderCodec<String, HexBookAsset> CODEC;
    private static AssetStore<String, HexBookAsset, DefaultAssetMap<String, HexBookAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected String itemId;
    protected int slotCount = 10;
    protected ModelParticle[] castingAuraParticles;
    protected ModelParticle[] craftingAuraParticles;
    protected String styleId;
    @Nullable
    protected String defaultsId;

    public static AssetStore<String, HexBookAsset, DefaultAssetMap<String, HexBookAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(HexBookAsset.class);
        }

        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, HexBookAsset> getAssetMap() {
        return (DefaultAssetMap<String, HexBookAsset>) getAssetStore().getAssetMap();
    }

    private HexBookAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getItemId() {
        return this.itemId;
    }

    public int getSlotCount() {
        return this.slotCount;
    }

    public ModelParticle[] getCastingAuraParticles() {
        return this.castingAuraParticles;
    }

    public ModelParticle[] getCraftingAuraParticles() {
        return this.craftingAuraParticles;
    }

    public String getStyleId() {
        return this.styleId;
    }

    public HexStyleAsset getStyle() {
        if (this.styleId == null) return null;
        return HexStyleAsset.getAssetMap().getAsset(this.styleId);
    }

    @Nullable
    public HexConfigAsset getDefaults() {
        if (this.defaultsId == null) return null;
        return HexConfigAsset.getAssetMap().getAsset(this.defaultsId);
    }

    public HexColors getColors() {
        HexStyleAsset style = getStyle();
        if (style == null) return null;
        HexColors c = new HexColors();
        if (style.getPrimaryColor() != null) c.setPrimaryColor(style.getPrimaryColor().clone());
        if (style.getSecondaryColor() != null) c.setSecondaryColor(style.getSecondaryColor().clone());
        c.setPrimaryAlpha(style.getAlpha());
        return c;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(HexBookAsset.class, HexBookAsset::new, Codec.STRING,
                        (glyphAsset, s) -> glyphAsset.id = s,
                        (glyphAsset) -> glyphAsset.id,
                        (asset, data) -> asset.data = data,
                        (asset) -> asset.data)
                .appendInherited(new KeyedCodec<>("MaxGlyphs", Codec.INTEGER),
                        (a, v) -> a.slotCount = v,
                        a -> a.slotCount,
                        (a, p) -> a.slotCount = p.slotCount)
                .documentation("Number of hex pages (imbuement slots) this book exposes in Selection Mode.")
                .add()
                .appendInherited(new KeyedCodec<>("CastingAuraParticles", ModelParticle.ARRAY_CODEC),
                        (a, v) -> a.castingAuraParticles = v,
                        a -> a.castingAuraParticles,
                        (a, p) -> a.castingAuraParticles = p.castingAuraParticles)
                .add()
                .appendInherited(new KeyedCodec<>("CraftingAuraParticles", ModelParticle.ARRAY_CODEC),
                        (a, v) -> a.craftingAuraParticles = v,
                        a -> a.craftingAuraParticles,
                        (a, p) -> a.craftingAuraParticles = p.craftingAuraParticles)
                .add()
                .appendInherited(new KeyedCodec<>("Style", HexStyleAsset.CHILD_ASSET_CODEC),
                        (a, v) -> a.styleId = v,
                        a -> a.styleId,
                        (a, p) -> a.styleId = p.styleId)
                .addValidatorLate(() -> HexStyleAsset.VALIDATOR_CACHE.getValidator().late())
                .add()
                .appendInherited(new KeyedCodec<>("Defaults", HexConfigAsset.CHILD_ASSET_CODEC),
                        (a, v) -> a.defaultsId = v,
                        a -> a.defaultsId,
                        (a, p) -> a.defaultsId = p.defaultsId)
                .addValidatorLate(() -> HexConfigAsset.VALIDATOR_CACHE.getValidator().late())
                .documentation("Optional cast overrides applied when this book is wielded. Same shape as ImbuementProfileAsset.Defaults.")
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(HexBookAsset::getAssetStore));
    }
}
