package com.riprod.hexcode.core.common.appearance;

import javax.annotation.Nullable;

import com.hypixel.hytale.protocol.PlayerSkin;

public record AppearanceLayer(
        @Nullable String modelAssetId,
        @Nullable PlayerSkin skin,
        @Nullable Float scale,
        long sequence) {

    public static AppearanceLayer ofScale(float scale) {
        return new AppearanceLayer(null, null, scale, 0L);
    }

    public static AppearanceLayer ofModel(String modelAssetId, @Nullable PlayerSkin skin) {
        return new AppearanceLayer(modelAssetId, skin, null, 0L);
    }

    AppearanceLayer withSequence(long value) {
        return new AppearanceLayer(modelAssetId, skin, scale, value);
    }
}
