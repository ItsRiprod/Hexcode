package com.riprod.hexcode.core.common.appearance;

import javax.annotation.Nullable;

import com.hypixel.hytale.protocol.PlayerSkin;

/**
 * A single appearance layer rendered on the player
 * An appearance layer will prefer a blockAssetId over a modelAssetId - though
 * only one should be present at a time regardless
 */
public record AppearanceLayer(
        @Nullable String modelAssetId,
        @Nullable String blockAssetId,
        @Nullable PlayerSkin skin,
        @Nullable String nameplate,
        @Nullable Float baseScale,
        @Nullable Float scaleMultiplier,
        long sequence) {

    public static final String NAMEPLATE_HIDDEN = "";

    public static AppearanceLayer ofScale(float scaleMultiplier) {
        return new AppearanceLayer(null, null, null, null, null, scaleMultiplier, 0L);
    }

    public static AppearanceLayer ofModel(String modelAssetId, @Nullable PlayerSkin skin,
            @Nullable String nameplate, @Nullable Float baseScale) {
        return new AppearanceLayer(modelAssetId, null, skin, nameplate, baseScale, null, 0L);
    }

    public static AppearanceLayer ofBlock(String blockTypeId, @Nullable PlayerSkin skin,
            @Nullable String nameplate, @Nullable Float baseScale) {
        return new AppearanceLayer(null, blockTypeId, skin, nameplate, baseScale, null, 0L);
    }

    public static AppearanceLayer ofNameplateHidden() {
        return new AppearanceLayer(null, null, null, NAMEPLATE_HIDDEN, null, null, 0L);
    }

    AppearanceLayer withSequence(long value) {
        return new AppearanceLayer(modelAssetId, blockAssetId, skin, nameplate, baseScale, scaleMultiplier, value);
    }
}
