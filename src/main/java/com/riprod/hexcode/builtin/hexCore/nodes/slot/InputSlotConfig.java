package com.riprod.hexcode.builtin.hexCore.nodes.slot;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.node.NodeInterface;

public final class InputSlotConfig extends SlotConfig {
    public static final String TYPE = "Input";

    public static final BuilderCodec<InputSlotConfig> CODEC = BuilderCodec
            .builder(InputSlotConfig.class, InputSlotConfig::new, SlotConfig.BASE_CODEC)
            .appendInherited(new KeyedCodec<>("DefaultValue", Codec.DOUBLE),
                    (c, v) -> c.defaultValue = v,
                    c -> c.defaultValue,
                    (c, p) -> c.defaultValue = p.defaultValue)
            .add()
            .build();

    @Nullable
    private Double defaultValue;

    @Override
    @Nullable
    public Double getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public Slot create() {
        return new LinkSlot();
    }

    @Override
    public NodeInterface handler() {
        return SlotNodeHandler.INSTANCE;
    }

    @Override
    public boolean defaultUnique() {
        return true;
    }
}
