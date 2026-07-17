package com.riprod.hexcode.builtin.hexCore.nodes.slot;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.node.NodeInterface;

public final class BooleanSlotConfig extends SlotConfig {
    public static final String TYPE = "Boolean";

    public static final BuilderCodec<BooleanSlotConfig> CODEC = BuilderCodec
            .builder(BooleanSlotConfig.class, BooleanSlotConfig::new, SlotConfig.BASE_CODEC)
            .appendInherited(new KeyedCodec<>("DefaultValue", Codec.DOUBLE),
                    (c, v) -> c.defaultValue = v, c -> c.defaultValue,
                    (c, p) -> c.defaultValue = p.defaultValue)
            .add()
            .appendInherited(new KeyedCodec<>("PositiveLabel", Codec.STRING),
                    (c, v) -> c.positiveLabel = v, c -> c.positiveLabel,
                    (c, p) -> c.positiveLabel = p.positiveLabel)
            .add()
            .appendInherited(new KeyedCodec<>("NeutralLabel", Codec.STRING),
                    (c, v) -> c.neutralLabel = v, c -> c.neutralLabel,
                    (c, p) -> c.neutralLabel = p.neutralLabel)
            .add()
            .appendInherited(new KeyedCodec<>("NegativeLabel", Codec.STRING),
                    (c, v) -> c.negativeLabel = v, c -> c.negativeLabel,
                    (c, p) -> c.negativeLabel = p.negativeLabel)
            .add()
            .appendInherited(new KeyedCodec<>("PositiveDescription", Codec.STRING),
                    (c, v) -> c.positiveDescription = v, c -> c.positiveDescription,
                    (c, p) -> c.positiveDescription = p.positiveDescription)
            .add()
            .appendInherited(new KeyedCodec<>("NeutralDescription", Codec.STRING),
                    (c, v) -> c.neutralDescription = v, c -> c.neutralDescription,
                    (c, p) -> c.neutralDescription = p.neutralDescription)
            .add()
            .appendInherited(new KeyedCodec<>("NegativeDescription", Codec.STRING),
                    (c, v) -> c.negativeDescription = v, c -> c.negativeDescription,
                    (c, p) -> c.negativeDescription = p.negativeDescription)
            .add()
            .build();

    @Nullable
    private Double defaultValue;
    @Nullable
    private String positiveLabel;
    @Nullable
    private String neutralLabel;
    @Nullable
    private String negativeLabel;
    @Nullable
    private String positiveDescription;
    @Nullable
    private String neutralDescription;
    @Nullable
    private String negativeDescription;

    @Override
    @Nullable
    public Double getDefaultValue() {
        return this.defaultValue;
    }

    @Nullable
    public String labelFor(BooleanSlotState state) {
        return switch (state) {
            case NEGATIVE -> this.negativeLabel;
            case NEUTRAL -> this.neutralLabel;
            case POSITIVE -> this.positiveLabel;
        };
    }

    @Nullable
    public String descriptionFor(BooleanSlotState state) {
        return switch (state) {
            case NEGATIVE -> this.negativeDescription;
            case NEUTRAL -> this.neutralDescription;
            case POSITIVE -> this.positiveDescription;
        };
    }

    @Override
    public Slot create() {
        return new BooleanSlot();
    }

    @Override
    public NodeInterface handler() {
        return BooleanSlotHandler.INSTANCE;
    }

    @Override
    public boolean defaultUnique() {
        return true;
    }
}
