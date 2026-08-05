package com.riprod.hexcode.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.configly.Configly;
import com.riprod.configly.Config;
import javax.annotation.Nonnull;

public final class HexcodeConfig extends Config {

    @Nonnull
    public static final String TYPE = "Hexcode";

    @Nonnull
    public static final HexcodeConfig DEFAULTS = new HexcodeConfig();

    @Nonnull
    public static final BuilderCodec<HexcodeConfig> CODEC = BuilderCodec.builder(HexcodeConfig.class, HexcodeConfig::new)
        .append(new KeyedCodec<>("RespectPVP", Codec.BOOLEAN),
            (config, b) -> config.respectPvp = b,
            config -> config.respectPvp)
        .documentation("Whether Hexcode spells honour the server PVP setting")
        .add()
        .build();

    private boolean respectPvp = true;

    private HexcodeConfig() {
    }

    @Nonnull
    public static HexcodeConfig get() {
        return Configly.getOrElse(TYPE, HexcodeConfig.class, DEFAULTS);
    }

    public boolean respectsPvp() {
        return respectPvp;
    }
}
