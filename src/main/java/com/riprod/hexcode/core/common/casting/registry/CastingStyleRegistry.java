package com.riprod.hexcode.core.common.casting.registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.casting.component.CastingStyle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CastingStyleRegistry {

    private static final Map<String, CastingStyle> styles = new HashMap<>();

    @Nullable
    private static String defaultStyleId;

    private CastingStyleRegistry() {
    }

    public static void register(@Nonnull CastingStyle style) {
        styles.put(style.getStyleId(), style);
    }

    public static void setDefault(@Nonnull String styleId) {
        defaultStyleId = styleId;
    }

    @Nullable
    public static CastingStyle get(@Nonnull String styleId) {
        return styles.get(styleId);
    }

    @Nonnull
    public static Set<String> keys() {
        return new HashSet<>(styles.keySet());
    }

    @Nonnull
    public static CastingStyle getOrDefault(@Nonnull String styleId) {
        CastingStyle style = styles.get(styleId);
        if (style == null && defaultStyleId != null) {
            style = styles.get(defaultStyleId);
        }
        if (style == null) {
            throw new IllegalStateException(
                    "no casting style for '" + styleId + "' and no registered default");
        }
        return style;
    }
}
