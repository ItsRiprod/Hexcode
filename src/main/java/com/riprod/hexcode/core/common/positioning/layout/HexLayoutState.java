package com.riprod.hexcode.core.common.positioning.layout;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record HexLayoutState(@Nonnull HexLayout layout, @Nonnull LayoutParams params,
        @Nonnull Map<String, Placement> placements) {

    @Nullable
    public Placement get(String glyphId) {
        return placements.get(glyphId);
    }
}
