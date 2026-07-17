package com.riprod.hexcode.core.common.imbuement.registry;

import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.triggers.registry.TriggerRegistry;

public final class ImbuementSlotKeyValidator implements Validator<Map<String, PedestalSlot>> {

    public static final ImbuementSlotKeyValidator INSTANCE = new ImbuementSlotKeyValidator();

    private ImbuementSlotKeyValidator() {
    }

    @Override
    public void accept(Map<String, PedestalSlot> slots, @Nonnull ValidationResults results) {
        if (slots == null || slots.isEmpty()) return;
        for (String key : slots.keySet()) {
            if (key == null || key.isEmpty()) continue;
            if (!TriggerRegistry.isProfileSlotEligible(key)) {
                results.fail("Unknown trigger slot key '" + key + "'. Registered: "
                        + String.join(", ", TriggerRegistry.keys()));
            }
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Slot keys must match a registered Trigger id (TriggerRegistry).");
    }
}
