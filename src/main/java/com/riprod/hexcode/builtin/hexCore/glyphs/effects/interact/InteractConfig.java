package com.riprod.hexcode.builtin.hexCore.glyphs.effects.interact;

import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class InteractConfig extends GlyphConfig {

    public static final InteractConfig DEFAULTS = new InteractConfig();

    @Nullable
    private String reachProxyItem;

    @Nullable
    public String getReachProxyItem() {
        return reachProxyItem;
    }

    public static final BuilderCodec<InteractConfig> CODEC = BuilderCodec
            .builder(InteractConfig.class, InteractConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ReachProxyItem",
                    new ContainedAssetCodec<>(Item.class, Item.CODEC), true),
                    (c, v) -> c.reachProxyItem = v, c -> c.reachProxyItem)
            .addValidatorLate(() -> Item.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
