package com.riprod.hexcode.core.common.hexes.saved;

import com.hypixel.hytale.assetstore.AssetExtraInfo.Data;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.riprod.hexcode.core.common.hexes.codec.HexFieldCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class SavedHexAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, SavedHexAsset>> {
    public static final AssetBuilderCodec<String, SavedHexAsset> CODEC;
    private static AssetStore<String, SavedHexAsset, DefaultAssetMap<String, SavedHexAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected Data data;
    protected String id;
    protected Hex hex;
    protected String displayName;
    protected String description;
    protected String authorUuid;

    public static AssetStore<String, SavedHexAsset, DefaultAssetMap<String, SavedHexAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(SavedHexAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, SavedHexAsset> getAssetMap() {
        return (DefaultAssetMap<String, SavedHexAsset>) getAssetStore().getAssetMap();
    }

    private SavedHexAsset() {
    }

    public SavedHexAsset(String id, Hex hex, String displayName, String description, String authorUuid) {
        this.id = id;
        this.hex = hex;
        this.displayName = displayName;
        this.description = description;
        this.authorUuid = authorUuid;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Hex getHex() {
        return this.hex;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAuthorUuid() {
        return this.authorUuid;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(SavedHexAsset.class, SavedHexAsset::new, Codec.STRING,
                        (asset, s) -> asset.id = s,
                        (asset) -> asset.id,
                        (asset, d) -> asset.data = d,
                        (asset) -> asset.data)
                .appendInherited(new KeyedCodec<>("Hex", HexFieldCodec.CODEC),
                        (a, v) -> a.hex = v,
                        a -> a.hex,
                        (a, p) -> { if (p.hex != null) a.hex = p.hex.clone(); })
                .add()
                .appendInherited(new KeyedCodec<>("DisplayName", Codec.STRING),
                        (a, v) -> a.displayName = v,
                        a -> a.displayName,
                        (a, p) -> a.displayName = p.displayName)
                .add()
                .appendInherited(new KeyedCodec<>("Description", Codec.STRING),
                        (a, v) -> a.description = v,
                        a -> a.description,
                        (a, p) -> a.description = p.description)
                .add()
                .appendInherited(new KeyedCodec<>("AuthorUuid", Codec.STRING),
                        (a, v) -> a.authorUuid = v,
                        a -> a.authorUuid,
                        (a, p) -> a.authorUuid = p.authorUuid)
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(SavedHexAsset::getAssetStore));
    }
}
