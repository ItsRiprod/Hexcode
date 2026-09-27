package com.riprod.hexcode.core.common.profiles.assets.filters;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.core.common.profiles.assets.ItemFilter;

public class ItemIdFilter extends ItemFilter  {
    public static final String ID = "ItemIdFilter";

    @Nonnull
    public static final BuilderCodec<ItemIdFilter> CODEC = BuilderCodec.builder(ItemIdFilter.class, ItemIdFilter::new, ABSTRACT_CODEC)
        .build();

    @Override
    public boolean test(ItemStack item) {
        throw new UnsupportedOperationException("Unimplemented method 'test'");
    }

}
