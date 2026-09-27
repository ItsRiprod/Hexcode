package com.riprod.hexcode.core.common.profiles.utils;

import com.riprod.hexcode.core.common.profiles.assets.ItemFilter;
import com.riprod.hexcode.core.common.profiles.assets.filters.ArmorSlotFilter;
import com.riprod.hexcode.core.common.profiles.assets.filters.CategoryFilter;
import com.riprod.hexcode.core.common.profiles.assets.filters.ItemIdFilter;
import com.riprod.hexcode.core.common.profiles.assets.filters.TagFilter;

public class FilterUtils {
    // central setup function for registering the filters - can/should be replicated in builtin/
    public static void setup() {
        ItemFilter.CODEC.register(CategoryFilter.ID, CategoryFilter.class, CategoryFilter.CODEC);
        ItemFilter.CODEC.register(ArmorSlotFilter.ID, ArmorSlotFilter.class, ArmorSlotFilter.CODEC);
        ItemFilter.CODEC.register(ItemIdFilter.ID, ItemIdFilter.class, ItemIdFilter.CODEC);
        ItemFilter.CODEC.register(TagFilter.ID, TagFilter.class, TagFilter.CODEC);
    }
}
