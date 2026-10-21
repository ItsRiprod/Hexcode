package com.riprod.hexcode.core.common.glyphs.variables;

import javax.annotation.Nonnull;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class NumberVar extends HexVar {
    private Double number;

    public NumberVar() {
    }

    public NumberVar(Double number) {
        this.number = number;
    }

    public NumberVar(Float number) {
        this.number = number == null ? null : number.doubleValue();
    }

    public NumberVar(int number) {
        this.number = (double) number;
    }

    public Double getValue() {
        return number;
    }

    @Override
    public VarType type() {
        return VarType.NUMBER;
    }

    @Override
    public HexVar copy() {
        return new NumberVar(number);
    }

    @Override
    public Double scalar() {
        return number;
    }

    @Override
    @Nonnull
    public Vector3d position(ComponentAccessor<EntityStore> accessor) {
        double n = number == null ? 0.0 : number;
        return new Vector3d(n);
    }

    @Override
    @Nonnull
    public Rotation3f rotation(ComponentAccessor<EntityStore> accessor) {
        float n = number == null ? 0f : number.floatValue();
        return new Rotation3f(n, n, n);
    }

    @Override
    public String describe() {
        return "NumberVar: " + number;
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof NumberVar nb) {
            return Double.compare(this.number, nb.number) == 0;
        }
        return super.equalTo(other);
    }

    @Override
    public int compareTo(HexVar other) {
        if (other instanceof NumberVar nb) {
            return Double.compare(this.number, nb.number);
        }
        return super.compareTo(other);
    }

    @Override
    public String toString() {
        return String.valueOf(number.intValue());
    }

    public static final BuilderCodec<NumberVar> CODEC = BuilderCodec
            .builder(NumberVar.class, NumberVar::new, HexVar.BASE_CODEC)
            .append(new KeyedCodec<>("Value", Codec.DOUBLE),
                    (v, num) -> v.number = num,
                    v -> v.number)
            .add()
            .build();

    static {
        HexVar.CODEC.register("Number", NumberVar.class, NumberVar.CODEC);
    }
}
