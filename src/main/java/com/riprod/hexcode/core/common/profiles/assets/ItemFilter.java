package com.riprod.hexcode.core.common.profiles.assets;

import javax.annotation.Nonnull;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public abstract class ItemFilter implements JsonAssetWithMap<String, DefaultAssetMap<String, ItemFilter>> {

    public static final AssetCodecMapCodec<String, ItemFilter> CODEC = new AssetCodecMapCodec<>(
            Codec.STRING,
            (t, k) -> t.id = k,
            t -> t.id,
            (t, data) -> t.data = data,
            t -> t.data);

    /**
     * The codec used to parse child {@link ItemFilter} assets.
     */
    @Nonnull
    public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(ItemFilter.class,
            ItemFilter.CODEC);
    /**
     * The codec used to parse arrays of child {@link ItemFilter} assets.
     */
    @Nonnull
    public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(ItemFilter.CHILD_ASSET_CODEC,
            String[]::new);
    /**
     * The abstract codec used to parse common {@link ItemFilter} properties.
     */
    @Nonnull
    public static final BuilderCodec<ItemFilter> ABSTRACT_CODEC = BuilderCodec
            .abstractBuilder(ItemFilter.class)
            .build();

    /**
     * The validator cache for {@link ItemFilter} assets.
     */
    @Nonnull
    public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(
            new AssetKeyValidator<>(ItemFilter::getAssetStore));

    /**
     * The asset store for {@link ItemFilter} assets.
     */
    private static AssetStore<String, ItemFilter, DefaultAssetMap<String, ItemFilter>> ASSET_STORE;

    /**
     * @return the asset store for {@link ItemFilter} assets.
     */
    @Nonnull
    public static AssetStore<String, ItemFilter, DefaultAssetMap<String, ItemFilter>> getAssetStore() {
        if (ASSET_STORE == null)
            ASSET_STORE = AssetRegistry.getAssetStore(ItemFilter.class);
        return ASSET_STORE;
    }

    /**
     * @return the asset map for {@link ItemFilter} assets.
     */
    public static DefaultAssetMap<String, ItemFilter> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    /**
     * The unique identifier for this asset.
     */
    protected String id;
    /**
     * The additional asset information for this asset.
     */
    protected AssetExtraInfo.Data data;

    @Override
    public String getId() {
        return id;
    }


    // Actually do shit stuff
    
    public abstract boolean test(ItemStack item);

}
