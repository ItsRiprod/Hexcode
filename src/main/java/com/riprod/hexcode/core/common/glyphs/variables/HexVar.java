package com.riprod.hexcode.core.common.glyphs.variables;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract sealed class HexVar
        permits NumberVar, PositionVar, RotationVar, EntityVar, BlockVar, ColorVar {
    public static final CodecMapCodec<HexVar> CODEC = new CodecMapCodec<>("Type");
    public static final BuilderCodec<HexVar> BASE_CODEC = BuilderCodec.abstractBuilder(HexVar.class).build();

    public abstract Object getRawValue();

    public abstract Double toScalar();

    public abstract String describe();

    public abstract HexVar copy();

    public PositionVar toPosition(ComponentAccessor<EntityStore> accessor) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot convert to Position");
    }

    public RotationVar toRotation(ComponentAccessor<EntityStore> accessor) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot convert to Rotation");
    }

    public ColorVar toColor(ComponentAccessor<EntityStore> accessor) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot convert to Color");
    }

    public BlockVar toBlockVar(ComponentAccessor<EntityStore> accessor) {
        return null;
    }

    public final HexVar convertTo(Class<? extends HexVar> target, ComponentAccessor<EntityStore> accessor) {
        if (target == this.getClass())
            return this;
        if (target == NumberVar.class)
            return new NumberVar(toScalar());
        if (target == PositionVar.class)
            return toPosition(accessor);
        if (target == RotationVar.class)
            return toRotation(accessor);
        if (target == ColorVar.class)
            return toColor(accessor);
        if (target == BlockVar.class)
            return toBlockVar(accessor);
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " cannot convert to " + target.getSimpleName());
    }

    public HexVar resolveSelf(HexVar partner, ComponentAccessor<EntityStore> accessor) {
        return this;
    }

    public boolean equalTo(HexVar other) {
        if (other == null)
            return false;
        return Double.compare(this.toScalar(), other.toScalar()) == 0;
    }

    public int compareTo(HexVar other) {
        if (other == null)
            return 1;
        return Double.compare(this.toScalar(), other.toScalar());
    }
}
