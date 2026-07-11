package com.riprod.hexcode.builtin.hexCore.nodes.slot;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
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

    public BooleanSlotState getState() {
        return this.state != null ? this.state : BooleanSlotState.NEUTRAL;
    }

    public void setState(BooleanSlotState state) {
        this.state = state;
    }

    @Override
    public void hydrateFrom(SlotAsset asset, String key, Vector3f resolvedOffset, String glyphId) {
        super.hydrateFrom(asset, key, resolvedOffset, glyphId);
        if (this.state == null) {
            this.state = BooleanSlotState.fromDefault(asset.getDefaultValue());
        }
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
        return copy;
    }
}
