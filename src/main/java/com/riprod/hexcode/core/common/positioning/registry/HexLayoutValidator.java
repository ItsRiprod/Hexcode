package com.riprod.hexcode.core.common.positioning.registry;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

public final class HexLayoutValidator implements Validator<String> {

    public static final HexLayoutValidator INSTANCE = new HexLayoutValidator();

    private HexLayoutValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (!HexLayoutRegistry.keys().contains(key)) {
            results.fail("Unknown hex layout '" + key + "'. Registered: "
                    + String.join(", ", HexLayoutRegistry.keys()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered hexcode hex layout (HexcodeHexLayouts).");
    }
}
