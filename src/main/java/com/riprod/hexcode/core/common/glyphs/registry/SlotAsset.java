package com.riprod.hexcode.core.common.glyphs.registry;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import org.joml.Vector3f;

public class SlotAsset {
    private String label;
    private String description;
    @Nullable
    private Vector3f offset;
    @Nullable
    private Double defaultValue;
    private boolean unique = false;
    @Nullable
    private String styleId;
    @Nullable
    private Impact impact;

    public SlotAsset() {
    }

    public static SlotAsset of(String label, String description,
            @Nullable Vector3f offset, String styleId) {
        SlotAsset asset = new SlotAsset();
        asset.label = label;
        asset.description = description;
        asset.offset = offset;
        asset.styleId = styleId;
        return asset;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    @Nullable
    public Vector3f getOffset() {
        return offset;
    }

    @Nullable
    public Double getDefaultValue() {
        return defaultValue;
    }

    public boolean isUnique() {
        return unique;
    }

    @Nullable
    public String getStyleId() {
        return styleId;
    }

    @Nullable
    public Impact getImpact() {
        return impact;
    }

    public static final BuilderCodec<SlotAsset> CODEC = BuilderCodec
            .builder(SlotAsset.class, SlotAsset::new)
            .append(new KeyedCodec<>("Label", Codec.STRING),
                    (s, v) -> s.label = v, s -> s.label)
            .add()
            .append(new KeyedCodec<>("Description", Codec.STRING),
                    (s, v) -> s.description = v, s -> s.description)
            .add()
            .append(new KeyedCodec<>("DefaultValue", Codec.DOUBLE),
                    (s, v) -> s.defaultValue = v, s -> s.defaultValue)
            .add()
            .append(new KeyedCodec<>("Unique", Codec.BOOLEAN),
                    (s, v) -> s.unique = v != null && v, s -> s.unique)
            .add()
            .append(new KeyedCodec<>("Style", Codec.STRING),
                    (s, v) -> s.styleId = v, s -> s.styleId)
            .addValidatorLate(() -> SlotStyleAsset.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("Impact", Impact.CODEC),
                    (s, v) -> s.impact = v, s -> s.impact)
            .add()
            .build();
}
