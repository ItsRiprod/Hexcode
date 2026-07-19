package com.riprod.hexcode.builtin.hexCore.nodes.slot;

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

public final class BooleanSlot extends Slot {
    public static final BuilderCodec<BooleanSlot> CODEC = BuilderCodec
            .builder(BooleanSlot.class, BooleanSlot::new, Slot.BASE_CODEC)
            .append(new KeyedCodec<>("State", new EnumCodec<>(BooleanSlotState.class), true),
                    (s, v) -> s.state = v,
                    s -> s.state)
            .add()
            .build();

    @Nullable
    private BooleanSlotState state;

    private transient BooleanSlotConfig config;

    public BooleanSlotState getState() {
        return this.state != null ? this.state : BooleanSlotState.NEUTRAL;
    }

    public void setState(BooleanSlotState state) {
        this.state = state;
    }

    @Override
    public void hydrateFrom(SlotConfig config, String key, Vector3f resolvedOffset) {
        super.hydrateFrom(config, key, resolvedOffset);
        if (config instanceof BooleanSlotConfig booleanConfig) {
            this.config = booleanConfig;
            if (this.state == null) {
                this.state = BooleanSlotState.fromDefault(booleanConfig.getDefaultValue());
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
            this.state = BooleanSlotState.fromOrdinal(state[0] & 0xFF);
        }
    }

    @Override
    @Nullable
    public HexVar inlineValue() {
        return this.state != null ? new NumberVar(this.state.value()) : null;
    }

    public DebugShape currentShape() {
        return getState() == BooleanSlotState.NEUTRAL ? DebugShape.Cylinder : DebugShape.Cone;
    }

    public boolean isInverted() {
        return getState() == BooleanSlotState.NEGATIVE;
    }

    @Override
    public Slot clone() {
        BooleanSlot copy = new BooleanSlot();
        copyBaseState(copy);
        copy.state = this.state;
        copy.config = this.config;
        return copy;
    }
}
