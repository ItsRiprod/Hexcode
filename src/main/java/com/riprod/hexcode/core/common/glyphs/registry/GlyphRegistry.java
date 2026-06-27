package com.riprod.hexcode.core.common.glyphs.registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GlyphRegistry {
    private static final Map<String, GlyphHandler> glyphs = new HashMap<>();
    private static boolean initialized = false;

    private GlyphRegistry() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
    }

    public static void register(@Nonnull GlyphHandler glyph) {
        GlyphHandler existing = glyphs.get(glyph.getId());
        if (existing != null) {
            throw new IllegalArgumentException("duplicate glyph handler id: " + glyph.getId());
        }
        glyphs.put(glyph.getId(), glyph);

        GlyphHandler.ConfigBinding<? extends GlyphConfig> binding = glyph.getConfigBinding();
        if (binding != null) {
            GlyphConfig.CODEC.register(glyph.getId(), binding.type(), binding.codec());
        }
    }

    @Nullable
    public static GlyphHandler get(@Nonnull String glyphId) {
        return glyphs.get(glyphId);
    }

    @Nonnull
    public static Map<String, GlyphHandler> getAll() {
        return Collections.unmodifiableMap(new HashMap<>(glyphs));
    }
}
