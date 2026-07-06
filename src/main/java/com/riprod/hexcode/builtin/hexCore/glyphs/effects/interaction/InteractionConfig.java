package com.riprod.hexcode.builtin.hexCore.glyphs.effects.interaction;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class InteractionConfig extends GlyphConfig {

    public static final InteractionConfig DEFAULTS = new InteractionConfig();

    @Nullable
    private String rootInteractionId;
    private InteractionType interactionType = InteractionType.Use;
    private InteractionAnchor anchor = InteractionAnchor.SOURCE;
    private boolean useRules = false;
    private boolean awaitCompletion = false;
    private boolean writeResult = true;
    private Map<String, String> interactionVars = new HashMap<>();

    @Nullable
    public String getRootInteractionId() {
        return rootInteractionId;
    }

    @Nullable
    public RootInteraction resolveRootInteraction() {
        if (rootInteractionId == null) {
            return null;
        }
        return RootInteraction.getAssetMap().getAsset(rootInteractionId);
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public InteractionAnchor getAnchor() {
        return anchor;
    }

    public boolean isUseRules() {
        return useRules;
    }

    public boolean isAwaitCompletion() {
        return awaitCompletion;
    }

    public boolean isWriteResult() {
        return writeResult;
    }

    public Map<String, String> getInteractionVars() {
        return interactionVars;
    }

    public static final BuilderCodec<InteractionConfig> CODEC = BuilderCodec
            .builder(InteractionConfig.class, InteractionConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("RootInteraction", RootInteraction.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.rootInteractionId = v, c -> c.rootInteractionId)
            .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("InteractionType", new EnumCodec<>(InteractionType.class), true),
                    (c, v) -> c.interactionType = v, c -> c.interactionType)
            .add()
            .append(new KeyedCodec<>("RunOn", new EnumCodec<>(InteractionAnchor.class), true),
                    (c, v) -> c.anchor = v, c -> c.anchor)
            .add()
            .append(new KeyedCodec<>("UseRules", Codec.BOOLEAN, true),
                    (c, v) -> c.useRules = v, c -> c.useRules)
            .add()
            .append(new KeyedCodec<>("AwaitCompletion", Codec.BOOLEAN, true),
                    (c, v) -> c.awaitCompletion = v, c -> c.awaitCompletion)
            .add()
            .append(new KeyedCodec<>("WriteResult", Codec.BOOLEAN, true),
                    (c, v) -> c.writeResult = v, c -> c.writeResult)
            .add()
            .append(new KeyedCodec<>("InteractionVars",
                    new MapCodec<>(RootInteraction.CHILD_ASSET_CODEC, HashMap::new), true),
                    (c, v) -> c.interactionVars = v, c -> c.interactionVars)
            .add()
            .build();
}
