package com.riprod.hexcode.core.common.glyphs.variables;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;

import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class ColorVar extends HexVar {
    private double r;
    private double g;
    private double b;
    private double a;

    public ColorVar() {
    }

    @Override
    public VarType type() {
        return VarType.POSITION;
    }

    public ColorVar(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public double getR() { return r; }
    public double getG() { return g; }
    public double getB() { return b; }
    public double getA() { return a; }

    @Override
    public HexVar copy() {
        return new ColorVar(r, g, b, a);
    }

    @Override
    public Double scalar() {
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    @Override
    public Vector3d position(ComponentAccessor<EntityStore> accessor) {
        return new Vector3d(r, g, b);
    }

    @Override
    public Rotation3fc rotation(ComponentAccessor<EntityStore> accessor) {
        return new Rotation3f(0, 0, 0);
    }

    @Override
    public String describe() {
        return String.format("ColorVar: rgba(%.2f, %.2f, %.2f, %.2f)", r, g, b, a);
    }

    @Override
    public boolean equalTo(HexVar other) {
        if (other instanceof ColorVar cb) {
            return Double.compare(r, cb.r) == 0
                    && Double.compare(g, cb.g) == 0
                    && Double.compare(b, cb.b) == 0
                    && Double.compare(a, cb.a) == 0;
        }
        return super.equalTo(other);
    }

    @Override
    public String toString() {
        return "ColorVar(" + r + ", " + g + ", " + b + ", " + a + ")";
    }

    public static final BuilderCodec<ColorVar> CODEC = BuilderCodec
            .builder(ColorVar.class, ColorVar::new, HexVar.BASE_CODEC)
            .append(new KeyedCodec<>("R", Codec.DOUBLE),
                    (v, val) -> v.r = val,
                    v -> v.r)
            .add()
            .append(new KeyedCodec<>("G", Codec.DOUBLE),
                    (v, val) -> v.g = val,
                    v -> v.g)
            .add()
            .append(new KeyedCodec<>("B", Codec.DOUBLE),
                    (v, val) -> v.b = val,
                    v -> v.b)
            .add()
            .append(new KeyedCodec<>("A", Codec.DOUBLE),
                    (v, val) -> v.a = val,
                    v -> v.a)
            .add()
            .build();

    static {
        HexVar.CODEC.register("Color", ColorVar.class, ColorVar.CODEC);
    }
}
