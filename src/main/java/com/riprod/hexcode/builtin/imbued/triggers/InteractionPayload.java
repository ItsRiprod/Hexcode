package com.riprod.hexcode.builtin.imbued.triggers;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record InteractionPayload(Ref<EntityStore> player,
                                  InteractionType interactionType,
                                  @Nullable ItemStack itemInHand,
                                  int activeHotbarSlot) {
}
