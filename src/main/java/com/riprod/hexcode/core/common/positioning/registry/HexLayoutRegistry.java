package com.riprod.hexcode.core.common.positioning.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.positioning.layout.HexLayout;

public final class HexLayoutRegistry {

    private static final Map<String, HexLayout> layouts = new HashMap<>();

    @Nullable
    private static String defaultLayoutId;

    private HexLayoutRegistry() {
    }

    public static void register(@Nonnull HexLayout layout) {
        layouts.put(layout.getLayoutId(), layout);
    }

    public static void setDefault(@Nonnull String layoutId) {
        defaultLayoutId = layoutId;
    }

    @Nullable
    public static HexLayout get(@Nonnull String layoutId) {
        return layouts.get(layoutId);
    }

    @Nonnull
    public static Set<String> keys() {
        return new HashSet<>(layouts.keySet());
    }

    @Nonnull
    public static HexLayout getOrDefault(@Nullable String layoutId) {
        var layout = layoutId != null ? layouts.get(layoutId) : null;
        if (layout == null && defaultLayoutId != null) {
            layout = layouts.get(defaultLayoutId);
        }
        if (layout == null) {
            throw new IllegalStateException("no hex layout for '" + layoutId + "' and no registered default");
        }
        return layout;
    }
}
