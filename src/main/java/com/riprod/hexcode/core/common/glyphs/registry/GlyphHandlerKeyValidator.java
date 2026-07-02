package com.riprod.hexcode.core.common.glyphs.registry;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

public final class GlyphHandlerKeyValidator implements Validator<String> {

    public static final GlyphHandlerKeyValidator INSTANCE = new GlyphHandlerKeyValidator();

    private GlyphHandlerKeyValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (GlyphRegistry.get(key) == null) {
            results.fail("Unknown glyph handler '" + key + "'. Registered: "
                    + String.join(", ", GlyphRegistry.getAll().keySet()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered hexcode glyph handler (HexcodeGlyphHandlers).");
    }
}
