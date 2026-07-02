package com.riprod.hexcode.core.common.glyphs.registry;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.riprod.hexcode.core.common.node.NodeHandlerKeyValidator;

public class SlotStyleAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, SlotStyleAsset>> {
    public static final AssetBuilderCodec<String, SlotStyleAsset> CODEC;
    private static AssetStore<String, SlotStyleAsset, DefaultAssetMap<String, SlotStyleAsset>> ASSET_STORE;
    public static final ValidatorCache<String> VALIDATOR_CACHE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected Vector3f color = new Vector3f(1f, 1f, 1f);
    protected DebugShape shape = DebugShape.Cube;
    protected String nodeHandlerId = "slot.standard";

    public static AssetStore<String, SlotStyleAsset, DefaultAssetMap<String, SlotStyleAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(SlotStyleAsset.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, SlotStyleAsset> getAssetMap() {
        return (DefaultAssetMap<String, SlotStyleAsset>) getAssetStore().getAssetMap();
    }

    private SlotStyleAsset() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Vector3f getColor() {
        return this.color;
    }

    public DebugShape getShape() {
        return this.shape;
    }

    public String getNodeHandlerId() {
        return this.nodeHandlerId;
    }

    static {
        CODEC = AssetBuilderCodec
                .builder(SlotStyleAsset.class, SlotStyleAsset::new, Codec.STRING, (asset, s) -> {
                    asset.id = s;
                }, (asset) -> {
                    return asset.id;
                }, (asset, data) -> {
                    asset.data = data;
                }, (asset) -> {
                    return asset.data;
                })
                .append(new KeyedCodec<>("Color", Codec.FLOAT_ARRAY),
                        (a, v) -> {
                            if (v != null && v.length >= 3) a.color = new Vector3f(v[0], v[1], v[2]);
                        },
                        a -> new float[] { a.color.x, a.color.y, a.color.z })
                .add()
                .append(new KeyedCodec<>("Shape", new EnumCodec<>(DebugShape.class)),
                        (a, v) -> {
                            if (v != null) a.shape = v;
                        },
                        a -> a.shape)
                .add()
                .append(new KeyedCodec<>("NodeHandlerId", Codec.STRING),
                        (a, v) -> {
                            if (v != null) a.nodeHandlerId = v;
                        },
                        a -> a.nodeHandlerId)
                    .metadata(new UIEditor(new UIEditor.Dropdown("HexcodeNodeHandlers")))
                    .addValidatorLate(() -> NodeHandlerKeyValidator.INSTANCE.late())
                .add()
                .build();
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(SlotStyleAsset::getAssetStore));
    }
}
