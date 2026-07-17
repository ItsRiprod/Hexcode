package com.riprod.hexcode.core.common.node;

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
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;

public abstract class NodeConfig implements JsonAssetWithMap<String, DefaultAssetMap<String, NodeConfig>> {

    public static final AssetCodecMapCodec<String, NodeConfig> CODEC;
    public static final BuilderCodec<NodeConfig> BASE_CODEC;
    public static final Codec<String> CHILD_ASSET_CODEC;
    public static final ValidatorCache<String> VALIDATOR_CACHE;
    private static AssetStore<String, NodeConfig, DefaultAssetMap<String, NodeConfig>> ASSET_STORE;

    protected AssetExtraInfo.Data data;
    protected String id;
    protected Color color = new Color((byte) 153, (byte) 153, (byte) 153);
    protected DebugShape shape = DebugShape.Cube;

    public static AssetStore<String, NodeConfig, DefaultAssetMap<String, NodeConfig>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(NodeConfig.class);
        }
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, NodeConfig> getAssetMap() {
        return (DefaultAssetMap<String, NodeConfig>) getAssetStore().getAssetMap();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public abstract NodeInterface handler();

    public Color getColor() {
        return this.color;
    }

    public DebugShape getShape() {
        return this.shape;
    }

    static {
        CODEC = new AssetCodecMapCodec<>("Type", Codec.STRING,
                (a, s) -> a.id = s,
                a -> a.id,
                (a, data) -> a.data = data,
                a -> a.data);

        BASE_CODEC = BuilderCodec.abstractBuilder(NodeConfig.class)
                .appendInherited(new KeyedCodec<>("Color", ProtocolCodecs.COLOR),
                        (a, v) -> { if (v != null) a.color = v; }, a -> a.color,
                        (a, p) -> a.color = p.color)
                .add()
                .appendInherited(new KeyedCodec<>("Shape", new EnumCodec<>(DebugShape.class)),
                        (a, v) -> { if (v != null) a.shape = v; }, a -> a.shape,
                        (a, p) -> a.shape = p.shape)
                .add()
                .build();

        CHILD_ASSET_CODEC = new ContainedAssetCodec<>(NodeConfig.class, CODEC);
        VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(NodeConfig::getAssetStore));
    }
}
