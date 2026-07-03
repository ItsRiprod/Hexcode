package com.riprod.hexcode.core.common.casting.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.math.vector.Rotation3f;

import java.util.List;

public interface CastingStyle {

    @Nonnull
    String getStyleId();

    @Nonnull
    List<Rotation3f> getInitialPositions(int glyphCount, float lookYaw, float lookPitch);
}
