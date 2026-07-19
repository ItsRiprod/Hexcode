package com.riprod.hexcode.builtin.hexCore.glyphs.elements;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public abstract class ElementGlyphConfig extends GlyphConfig {

    public static final BuilderCodec<ElementGlyphConfig> BASE_CODEC = BuilderCodec
            .abstractBuilder(ElementGlyphConfig.class, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Efficiency", Codec.FLOAT, true),
                    (c, v) -> c.efficiency = v, c -> c.efficiency)
            .addValidator(Validators.greaterThanOrEqual(0.0f))
            .add()
            .append(new KeyedCodec<>("AffinityStat", Codec.STRING, true),
                    (c, v) -> c.affinityStat = v, c -> c.affinityStat)
            .addValidatorLate(() -> EntityStatType.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("AffinityScale", Codec.FLOAT, true),
                    (c, v) -> c.affinityScale = v, c -> c.affinityScale)
            .add()
            .append(new KeyedCodec<>("Resource", Codec.STRING, true),
                    (c, v) -> c.resource = v, c -> c.resource)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    protected float efficiency = 1.0f;

    @Nullable
    protected String affinityStat;

    protected float affinityScale = 1.0f;

    @Nullable
    protected String resource;

    public float getEfficiency() {
        return efficiency;
    }

    @Nullable
    public String getAffinityStat() {
        return affinityStat;
    }

    public float getAffinityScale() {
        return affinityScale;
    }

    @Nullable
    public String getResource() {
        return resource;
    }
}
