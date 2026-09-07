package com.riprod.hexcode.builtin.hexCore.imbuements.profiles;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.ItemDisplayMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.component.DecryptingState;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public final class ComponentProfile extends StaticSlotProfile {

    public static final BuilderCodec<ComponentProfile> CODEC =
            slotCodec(ComponentProfile.class, ComponentProfile::new);

    @Override
    public String getEntryContextId() {
        return DecryptingState.CONTEXT_ID;
    }

    @Override
    public int getEntryPriority() {
        return DecryptingState.PRIORITY;
    }

    @Override
    public ItemStack writeHex(ItemStack stored, String slotKey, @Nullable Hex hex) {
        ItemStack out = super.writeHex(stored, slotKey, hex);
        if (out == null || out.isEmpty()) {
            return out;
        }
        String name = hex != null ? hex.getDisplayName() : null;
        if (name == null || name.isBlank()) {
            return out.withMetadata(ItemDisplayMetadata.KEYED_CODEC, null);
        }
        return out.withMetadata(ItemDisplayMetadata.KEYED_CODEC,
                new ItemDisplayMetadata(Message.raw(name), null));
    }
}
