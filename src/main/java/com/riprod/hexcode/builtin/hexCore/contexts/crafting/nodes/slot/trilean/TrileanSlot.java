package com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public final class TrileanSlot extends Slot {
    public static final BuilderCodec<TrileanSlot> CODEC = BuilderCodec
            .builder(TrileanSlot.class, TrileanSlot::new, Slot.BASE_CODEC)
            .append(new KeyedCodec<>("State", new EnumCodec<>(TrileanSlotState.class), true),
                    (s, v) -> s.state = v,
                    s -> s.state)
            .add()
            .build();

    @Nullable
    private TrileanSlotState state;

    private transient TrileanSlotConfig config;

    public TrileanSlotState getState() {
        return this.state != null ? this.state : TrileanSlotState.NEUTRAL;
    }

    public void setState(TrileanSlotState state) {
        this.state = state;
    }

    @Override
    public void hydrateFrom(SlotConfig config, String key, Vector3f resolvedOffset) {
        super.hydrateFrom(config, key, resolvedOffset);
        if (config instanceof TrileanSlotConfig booleanConfig) {
            this.config = booleanConfig;
            if (this.state == null) {
                this.state = TrileanSlotState.fromDefault(booleanConfig.getDefaultValue());
            }
        }
    }

    @Override
    public String displayLabel() {
        String stateLabel = this.config != null ? this.config.labelFor(getState()) : null;
        return stateLabel != null ? stateLabel : super.displayLabel();
    }

    @Override
    public String displayDescription() {
        String stateDescription = this.config != null ? this.config.descriptionFor(getState()) : null;
        return stateDescription != null ? stateDescription : super.displayDescription();
    }

    @Override
    @Nullable
    public byte[] encodeState() {
        return this.state != null ? new byte[] { (byte) this.state.ordinal() } : null;
    }

    @Override
    public void decodeState(byte[] state) {
        if (state.length > 0) {
            this.state = TrileanSlotState.fromOrdinal(state[0] & 0xFF);
        }
    }

    @Override
    @Nullable
    public HexVar inlineValue() {
        return this.state != null ? new NumberVar(this.state.value()) : null;
    }

    public DebugShape currentShape() {
        return getState() == TrileanSlotState.NEUTRAL ? DebugShape.Cylinder : DebugShape.Cone;
    }

    public boolean isInverted() {
        return getState() == TrileanSlotState.NEGATIVE;
    }

    @Override
    public Slot clone() {
        TrileanSlot copy = new TrileanSlot();
        copyBaseState(copy);
        copy.state = this.state;
        copy.config = this.config;
        return copy;
    }
}
