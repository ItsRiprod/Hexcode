package com.riprod.hexcode.builtin.ums.validators;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.riprod.hexcode.builtin.ums.registry.UmsInteractionRegistry;

public final class UmsInteractionKeyValidator implements Validator<String> {

    public static final UmsInteractionKeyValidator INSTANCE = new UmsInteractionKeyValidator();

    private UmsInteractionKeyValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (UmsInteractionRegistry.get(key) == null) {
            results.fail("Unknown ums interaction handler '" + key + "'. Registered: "
                    + String.join(", ", UmsInteractionRegistry.getAll().keySet()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered ums interaction handler (HexcodeUmsInteractionHandlers).");
    }
}
