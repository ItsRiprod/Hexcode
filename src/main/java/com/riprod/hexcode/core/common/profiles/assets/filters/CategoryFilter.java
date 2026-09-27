package com.riprod.hexcode.core.common.profiles.assets.filters;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemCategory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.core.common.profiles.assets.ItemFilter;

public class CategoryFilter extends ItemFilter {
    public static final String ID = "CategoryFilter";

    @Nonnull
    public static final BuilderCodec<CategoryFilter> CODEC = BuilderCodec
            .builder(CategoryFilter.class, CategoryFilter::new, ABSTRACT_CODEC)
            .appendInherited(new KeyedCodec<>("ExcludedCategories",
                    new ArrayCodec<>(Codec.STRING, String[]::new)),
                    (a, v) -> {
                        if (v != null)
                            a.excludedCategories = v;
                    },
                    a -> a.excludedCategories,
                    (a, p) -> a.excludedCategories = p.excludedCategories)
            .metadata(new UIEditor(new UIEditor.Dropdown("ItemCategories")))
            .addValidatorLate(() -> new ArrayValidator<>(ItemCategory.VALIDATOR_CACHE.getValidator().late()).late())
            .documentation(
                    "Optional. If any of an item's Categories matches an entry in this list, the profile rejects the item.")
            .add()
            .appendInherited(new KeyedCodec<>("CategoryId", Codec.STRING),
                    (a, v) -> {
                        if (v != null)
                            a.categoryId = v;
                    },
                    a -> a.categoryId,
                    (a, p) -> a.categoryId = p.categoryId)
            .metadata(new UIEditor(new UIEditor.Dropdown("ItemCategories")))
            .addValidatorLate(() -> ItemCategory.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    protected String categoryId;
    protected String[] excludedCategories = new String[0];

    @Override
    public boolean test(ItemStack item) {
        throw new UnsupportedOperationException("Unimplemented method 'test'");
    }
}
