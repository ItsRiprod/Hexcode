package com.riprod.hexcode.builtin.hexCore.components.component;

import java.util.List;

import com.riprod.hexcode.core.common.hexes.component.EncodingStroke;

public record ComponentCacheEntry(List<EncodingStroke> encoding, String canonicalPayload,
        String sourceRaw) {
}
