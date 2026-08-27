package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class AreaConfig extends GlyphConfig {

    public static final AreaConfig DEFAULTS = new AreaConfig();

    private double minAxisSize = 1.0;
    private double perBlockPrice = 0.002;
    private double displayPriceMultiplier = 0.1;
    @Nullable
    private Impact ratePriceImpact;

    public double getMinAxisSize() {
        return minAxisSize;
    }

    public double getPerBlockPrice() {
        return perBlockPrice;
    }

    public double getDisplayPriceMultiplier() {
        return displayPriceMultiplier;
    }

    @Nullable
    public Impact getRatePriceImpact() {
        return ratePriceImpact;
    }

    public static final BuilderCodec<AreaConfig> CODEC = BuilderCodec
            .builder(AreaConfig.class, AreaConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("MinAxisSize", Codec.DOUBLE, true),
                    (c, v) -> c.minAxisSize = v, c -> c.minAxisSize)
            .add()
            .append(new KeyedCodec<>("PerBlockPrice", Codec.DOUBLE, true),
                    (c, v) -> c.perBlockPrice = v, c -> c.perBlockPrice)
            .add()
            .append(new KeyedCodec<>("DisplayPriceMultiplier", Codec.DOUBLE, true),
                    (c, v) -> c.displayPriceMultiplier = v, c -> c.displayPriceMultiplier)
            .add()
            .append(new KeyedCodec<>("RatePriceImpact", Impact.CODEC, true),
                    (c, v) -> c.ratePriceImpact = v, c -> c.ratePriceImpact)
            .add()
            .build();
}
