package com.riprod.hexcode.builtin.hexCore.staffStyles;

import javax.annotation.Nonnull;

import org.joml.Vector3f;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.common.casting.component.CastingStyle;

import java.util.ArrayList;
import java.util.List;

public class SphereStyle implements CastingStyle {

    public static final String ID = "sphere";
    private static final float DEFAULT_DISTANCE = 3.0f;

    @Nonnull
    @Override
    public String getStyleId() {
        return ID;
    }

    @Nonnull
    @Override
    public List<Rotation3f> getInitialPositions(int glyphCount, float lookYaw, float lookPitch) {
        List<Rotation3f> positions = new ArrayList<>();

        if (glyphCount <= 0) {
            return positions;
        }

        if (glyphCount == 1) {
            positions.add(new Rotation3f(0f, (float) (Math.PI / 4), DEFAULT_DISTANCE));
            return positions;
        }

        float goldenAngle = (float) (Math.PI * (3.0 - Math.sqrt(5.0)));

        for (int i = 0; i < glyphCount; i++) {
            float y = (float) i / (glyphCount - 1);
            float pitch = (float) Math.asin(y);

            float yaw = goldenAngle * i;

            positions.add(new Rotation3f(pitch, yaw, DEFAULT_DISTANCE));
        }

        return positions;
    }
}
