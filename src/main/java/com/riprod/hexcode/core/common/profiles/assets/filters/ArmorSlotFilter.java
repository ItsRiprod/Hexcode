package com.riprod.hexcode.core.common.profiles.assets.filters;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.core.common.profiles.assets.ItemFilter;

public class ArmorSlotFilter extends ItemFilter {
    public static final String ID = "ArmorSlotFilter";

    @Nonnull
    public static final BuilderCodec<ArmorSlotFilter> CODEC = BuilderCodec
            .builder(ArmorSlotFilter.class, ArmorSlotFilter::new, ABSTRACT_CODEC)
            .appendInherited(new KeyedCodec<>("ArmorSlot", new EnumCodec<>(ItemArmorSlot.class)),
                    (a, v) -> a.armorSlot = v,
                    a -> a.armorSlot,
                    (a, p) -> a.armorSlot = p.armorSlot)
            .documentation(
                    "Optional. When set, this profile only matches armor items occupying the given slot (Head/Chest/Hands/Legs).")
            .add()
            .build();

    @Nullable
    protected ItemArmorSlot armorSlot;

    @Override
    public boolean test(ItemStack item) {
        throw new UnsupportedOperationException("Unimplemented method 'test'");
    }
}
