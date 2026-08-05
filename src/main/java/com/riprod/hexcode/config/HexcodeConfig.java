package com.riprod.hexcode.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.configly.Configly;
import com.riprod.configly.Config;
import javax.annotation.Nonnull;

public final class HexcodeConfig extends Config {

    @Nonnull
    public static final String TYPE = "Hexcode";

    @Nonnull
    public static final HexcodeConfig DEFAULTS = new HexcodeConfig();

    @Nonnull
    public static final BuilderCodec<HexcodeConfig> CODEC = BuilderCodec
            .builder(HexcodeConfig.class, HexcodeConfig::new)
            .append(new KeyedCodec<>("RespectPvp", Codec.BOOLEAN),
                    (config, b) -> config.respectPvp = b,
                    config -> config.respectPvp)
            .documentation("Whether Hexcode utility/status spells honour the server PVP setting - damage spells will always respect the PVP config.")
            .add()
            .append(new KeyedCodec<>("RespectClaims", Codec.BOOLEAN),
                    (config, b) -> config.respectClaims = b,
                    config -> config.respectClaims)
            .documentation("Whether Hexcode spells honour block protection - world block break/place "
                    + "settings, non-modifiable environments, and Break/PlaceBlockEvent vetoes from "
                    + "land-claim plugins")
            .add()
            .append(new KeyedCodec<>("MaxGlyphsPerTick", Codec.INTEGER),
                    (config, i) -> config.maxGlyphsPerTick = i,
                    config -> config.maxGlyphsPerTick)
            .documentation("Maximum glyphs every spell in a world may execute together in one tick. "
                    + "Work above this is deferred to a later tick, never dropped.")
            .addValidator(Validators.min(1))
            .add()
            .append(new KeyedCodec<>("MaxGlyphsPerCast", Codec.INTEGER),
                    (config, i) -> config.maxGlyphsPerCast = i,
                    config -> config.maxGlyphsPerCast)
            .documentation("Maximum glyphs a single spell may execute in one tick, so one wide spell cannot "
                    + "monopolise the world budget")
            .addValidator(Validators.min(1))
            .add()
            .afterDecode((config, extraInfo) -> {
                if (config.maxGlyphsPerCast > config.maxGlyphsPerTick) {
                    extraInfo.getValidationResults().warn("MaxGlyphsPerCast (" + config.maxGlyphsPerCast
                            + ") exceeds MaxGlyphsPerTick (" + config.maxGlyphsPerTick
                            + "); clamping MaxGlyphsPerCast to MaxGlyphsPerTick");
                    config.maxGlyphsPerCast = config.maxGlyphsPerTick;
                }
            })
            .build();

    private boolean respectPvp = true;
    private boolean respectClaims = true;
    private int maxGlyphsPerTick = 512;
    private int maxGlyphsPerCast = 128;

    private HexcodeConfig() {
    }

    @Nonnull
    public static HexcodeConfig get() {
        return Configly.getOrElse(TYPE, HexcodeConfig.class, DEFAULTS);
    }

    public boolean respectsPvp() {
        return respectPvp;
    }

    public boolean respectsClaims() {
        return respectClaims;
    }

    public int getMaxGlyphsPerTick() {
        return maxGlyphsPerTick;
    }

    public int getMaxGlyphsPerCast() {
        return maxGlyphsPerCast;
    }
}
