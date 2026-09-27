package com.riprod.hexcode.core.common.imbuement.utils;

import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.core.common.profiles.assets.ProfileAsset;

import javax.annotation.Nullable;

public class ImbuementProfileUtils {

    @Nullable
    public static ProfileAsset byCategory(String categoryId) {
        return byCategoryAndArmorSlot(categoryId, null);
    }

    @Nullable
    public static ProfileAsset byCategoryAndArmorSlot(String categoryId, @Nullable ItemArmorSlot armorSlot) {
        return byCategoryAndArmorSlot(categoryId, armorSlot, null);
    }

    @Nullable
    public static ProfileAsset byCategoryAndArmorSlot(String categoryId, @Nullable ItemArmorSlot armorSlot,
            @Nullable String[] itemCategoriesForExclusion) {
        if (categoryId == null) return null;
        ProfileAsset fallback = null;
        for (ProfileAsset profile : ProfileAsset.getAssetMap().getAssetMap().values()) {
            if (!categoryId.equals(profile.getCategoryId())) continue;
            if (isExcluded(profile, itemCategoriesForExclusion)) continue;
            ItemArmorSlot profileSlot = profile.getArmorSlot();
            if (profileSlot == null) {
                if (fallback == null) fallback = profile;
                continue;
            }
            if (armorSlot != null && profileSlot == armorSlot) return profile;
        }
        return fallback;
    }

    private static boolean isExcluded(ProfileAsset profile, @Nullable String[] itemCategories) {
        String[] excluded = profile.getExcludedCategories();
        if (excluded == null || excluded.length == 0) return false;
        if (itemCategories == null) return false;
        for (String c : itemCategories) {
            for (String e : excluded) {
                if (e.equals(c)) return true;
            }
        }
        return false;
    }

    @Nullable
    public static ProfileAsset first(@Nullable String[] categories) {
        return first(categories, null);
    }

    @Nullable
    public static ProfileAsset first(@Nullable String[] categories, @Nullable ItemArmorSlot armorSlot) {
        if (categories == null) return null;
        for (String category : categories) {
            ProfileAsset profile = byCategoryAndArmorSlot(category, armorSlot, categories);
            if (profile != null) return profile;
        }
        return null;
    }

    @Nullable
    public static ProfileAsset first(@Nullable ItemStack item) {
        if (item == null || item.isEmpty() || item.getItem() == null) return null;
        Item def = item.getItem();
        ItemArmorSlot armorSlot = null;
        ItemArmor armor = def.getArmor();
        if (armor != null) armorSlot = armor.getArmorSlot();
        return first(def.getCategories(), armorSlot);
    }
}
