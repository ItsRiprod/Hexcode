package com.riprod.hexcode.core.common.node;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

public final class NodeHandlerKeyValidator implements Validator<String> {

    public static final NodeHandlerKeyValidator INSTANCE = new NodeHandlerKeyValidator();

    private NodeHandlerKeyValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (!NodeRouter.keys().contains(key)) {
            results.fail("Unknown node handler '" + key + "'. Registered: "
                    + String.join(", ", NodeRouter.keys()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered hexcode node handler (HexcodeNodeHandlers).");
    }
}
