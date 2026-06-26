package com.riprod.hexcode.builtin.hexCore.staffStyles;

import javax.annotation.Nonnull;

import org.joml.Vector3f;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.riprod.hexcode.core.state.casting.component.CastingStyle;

import java.util.ArrayList;
import java.util.List;

public class ArcStyle implements CastingStyle {
    public static final String ID = "arc";
    private static final float DEFAULT_DISTANCE = 3.0f;
    private static final float ARC_PITCH = 0.0f;
    private static final float ARC_SPAN = (float) (2 * Math.PI / 3);

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
            positions.add(new Rotation3f(ARC_PITCH, lookYaw, DEFAULT_DISTANCE));
            return positions;
        }

        float startYaw = lookYaw - ARC_SPAN / 2;
        float angleStep = ARC_SPAN / (glyphCount - 1);

        for (int i = 0; i < glyphCount; i++) {
            float yaw = startYaw + angleStep * i;
            positions.add(new Rotation3f(ARC_PITCH, yaw, DEFAULT_DISTANCE));
        }

        return positions;
    }
}
