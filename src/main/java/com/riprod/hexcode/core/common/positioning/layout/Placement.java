package com.riprod.hexcode.core.common.positioning.layout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public record Placement(@Nonnull Glyph glyph, @Nullable String parentId, @Nonnull Vector3f offset, float scale,
        float spin) {

    public boolean isRoot() {
        return parentId == null;
    }
}
