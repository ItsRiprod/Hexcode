package com.riprod.hexcode.core.common.execution.component;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;

public class HexColors {

    private Color primaryColor;
    private Color secondaryColor;
    @Nullable
    private Float primaryAlpha;

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(Color primaryColor) {
        this.primaryColor = primaryColor;
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(Color secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    @Nullable
    public Float getPrimaryAlpha() {
        return primaryAlpha;
    }

    public void setPrimaryAlpha(@Nullable Float primaryAlpha) {
        this.primaryAlpha = primaryAlpha == null ? null : clamp01(primaryAlpha);
    }

    public void setOverride(double r, double g, double b, double a) {
        byte rb = (byte) Math.round(clamp01((float) r) * 255f);
        byte gb = (byte) Math.round(clamp01((float) g) * 255f);
        byte bb = (byte) Math.round(clamp01((float) b) * 255f);
        this.primaryColor = new Color(rb, gb, bb);
        this.primaryAlpha = clamp01((float) a);
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : v > 1f ? 1f : v;
    }

    public static Vector3f toVector3f(Color color) {
        return new Vector3f((color.red & 0xFF) / 255f, (color.green & 0xFF) / 255f, (color.blue & 0xFF) / 255f);
    }

    public static final BuilderCodec<HexColors> CODEC = BuilderCodec
            .builder(HexColors.class, HexColors::new)
            .append(new KeyedCodec<>("PrimaryColor", ProtocolCodecs.COLOR),
                    (c, v) -> c.primaryColor = v,
                    c -> c.primaryColor)
            .add()
            .append(new KeyedCodec<>("SecondaryColor", ProtocolCodecs.COLOR),
                    (c, v) -> c.secondaryColor = v,
                    c -> c.secondaryColor)
            .add()
            .append(new KeyedCodec<>("PrimaryAlpha", Codec.FLOAT),
                    (c, v) -> c.primaryAlpha = v,
                    c -> c.primaryAlpha)
            .add()
            .build();

    public HexColors clone() {
        HexColors copy = new HexColors();
        if (this.primaryColor != null) copy.primaryColor = this.primaryColor.clone();
        if (this.secondaryColor != null) copy.secondaryColor = this.secondaryColor.clone();
        copy.primaryAlpha = this.primaryAlpha;
        return copy;
    }
}
