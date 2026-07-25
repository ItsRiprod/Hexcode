package com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class MageArmorConfig extends GlyphConfig {

    public static final MageArmorConfig DEFAULTS = new MageArmorConfig();

    private String statusEffect = "Hexcode_Mage_Armor";

    public String getStatusEffect() {
        return statusEffect;
    }

    public static final BuilderCodec<MageArmorConfig> CODEC = BuilderCodec
            .builder(MageArmorConfig.class, MageArmorConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("StatusEffect", Codec.STRING, true),
                    (c, v) -> c.statusEffect = v, c -> c.statusEffect)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
