package com.riprod.hexcode.core.common.casting.registry;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

public final class CastingStyleValidator implements Validator<String> {

    public static final CastingStyleValidator INSTANCE = new CastingStyleValidator();

    private CastingStyleValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (!CastingStyleRegistry.keys().contains(key)) {
            results.fail("Unknown casting style '" + key + "'. Registered: "
                    + String.join(", ", CastingStyleRegistry.keys()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered hexcode casting style (HexcodeCastingStyles).");
    }
}
