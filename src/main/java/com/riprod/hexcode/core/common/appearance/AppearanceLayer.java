package com.riprod.hexcode.core.common.appearance;

import javax.annotation.Nullable;

import com.hypixel.hytale.protocol.PlayerSkin;

public record AppearanceLayer(
        @Nullable String modelAssetId,
        @Nullable PlayerSkin skin,
        @Nullable String nameplate,
        @Nullable Float baseScale,
        @Nullable Float scaleMultiplier,
        long sequence) {

    public static AppearanceLayer ofScale(float scaleMultiplier) {
        return new AppearanceLayer(null, null, null, null, scaleMultiplier, 0L);
    }

    public static AppearanceLayer ofModel(String modelAssetId, @Nullable PlayerSkin skin,
            @Nullable String nameplate, @Nullable Float baseScale) {
        return new AppearanceLayer(modelAssetId, skin, nameplate, baseScale, null, 0L);
    }

    AppearanceLayer withSequence(long value) {
        return new AppearanceLayer(modelAssetId, skin, nameplate, baseScale, scaleMultiplier, value);
    }
}
