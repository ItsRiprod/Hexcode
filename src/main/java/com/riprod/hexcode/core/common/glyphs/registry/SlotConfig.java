package com.riprod.hexcode.core.common.glyphs.registry;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.node.NodeConfig;

public abstract class SlotConfig extends NodeConfig {

    public static final BuilderCodec<SlotConfig> BASE_CODEC = BuilderCodec
            .abstractBuilder(SlotConfig.class, NodeConfig.BASE_CODEC)
            .appendInherited(new KeyedCodec<>("Label", Codec.STRING),
                    (a, v) -> { if (v != null) a.label = v; }, a -> a.label,
                    (a, p) -> a.label = p.label)
            .add()
            .appendInherited(new KeyedCodec<>("Description", Codec.STRING),
                    (a, v) -> { if (v != null) a.description = v; }, a -> a.description,
                    (a, p) -> a.description = p.description)
            .add()
            .appendInherited(new KeyedCodec<>("Unique", Codec.BOOLEAN),
                    (a, v) -> a.unique = v, a -> a.unique,
                    (a, p) -> a.unique = p.unique)
            .add()
            .appendInherited(new KeyedCodec<>("Impact", Impact.CODEC),
                    (a, v) -> a.impact = v, a -> a.impact,
                    (a, p) -> a.impact = p.impact)
            .add()
            .build();

    protected String label = "hexcode.glyphs.default.unlabeled";
    protected String description = "hexcode.glyphs.default.nodescription";
    @Nullable
    protected Boolean unique;
    @Nullable
    protected Impact impact;

    public abstract Slot create();

    public abstract boolean defaultUnique();

    public boolean isFlow() {
        return false;
    }

    @Nullable
    public Double getDefaultValue() {
        return null;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    @Nullable
    public Boolean getUnique() {
        return this.unique;
    }

    public boolean isUnique() {
        return this.unique != null ? this.unique : defaultUnique();
    }

    @Nullable
    public Impact getImpact() {
        return this.impact;
    }
}
