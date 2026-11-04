package com.riprod.hexcode.builtin.elementalism.composite;

import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.hexcode.builtin.elementalism.element.ElementTierIndex;
import com.riprod.hexcode.builtin.elementalism.status.StatusAsset;

public class CompositeAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, CompositeAsset>> {
    public static final AssetBuilderCodec<String, CompositeAsset> CODEC;
    private static AssetStore<String, CompositeAsset, DefaultAssetMap<String, CompositeAsset>> ASSET_STORE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected String existing;
    protected String[] incoming;
    protected CompositeOutcome entity;
    protected CompositeOutcome block;

    public static AssetStore<String, CompositeAsset, DefaultAssetMap<String, CompositeAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(CompositeAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, CompositeAsset> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    protected CompositeAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getExisting() {
        return this.existing;
    }

    public List<String> getIncoming() {
        return List.of(this.incoming);
    }

    @Nullable
    public CompositeOutcome getEntity() {
        return this.entity;
    }

    @Nullable
    public CompositeOutcome getBlock() {
        return this.block;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(CompositeAsset.class, CompositeAsset::new, Codec.STRING,
                        (a, s) -> a.id = s,
                        a -> a.id,
                        (a, data) -> a.data = data,
                        a -> a.data)
                .appendInherited(new KeyedCodec<>("Existing", Codec.STRING),
                        (a, v) -> a.existing = v, a -> a.existing,
                        (a, p) -> a.existing = p.existing)
                .addValidator(Validators.nonNull())
                .addValidator(StatusAsset.VALIDATOR_CACHE.getValidator())
                .documentation("Status already on the entity or block")
                .add()
                .appendInherited(new KeyedCodec<>("Incoming", Codec.STRING_ARRAY),
                        (a, v) -> a.incoming = v, a -> a.incoming,
                        (a, p) -> a.incoming = p.incoming)
                .addValidator(Validators.nonNull())
                .addValidator(Validators.nonEmptyArray())
                .addValidator(Validators.uniqueInArray())
                .addValidator(ElementTierIndex.VALIDATOR_CACHE.getArrayValidator())
                .documentation("Tier ids that trigger this composite when they hit the existing status")
                .add()
                .appendInherited(new KeyedCodec<>("Entity", CompositeOutcome.CODEC),
                        (a, v) -> a.entity = v, a -> a.entity,
                        (a, p) -> a.entity = p.entity)
                .addValidator(CompositeOutcome.ENTITY_VALIDATOR)
                .documentation("Outcome when the status is on an entity, absent means the incoming tier applies normally")
                .add()
                .appendInherited(new KeyedCodec<>("Block", CompositeOutcome.CODEC),
                        (a, v) -> a.block = v, a -> a.block,
                        (a, p) -> a.block = p.block)
                .addValidator(CompositeOutcome.BLOCK_VALIDATOR)
                .documentation("Outcome when the status is on a block, absent means the incoming tier applies normally")
                .add()
                .build();
    }
}
