package com.riprod.hexcode.core.common.imbuement.asset.profiles;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

// book pages are generated per-item from the wielded HexBookAsset slot count, not authored in JSON
public final class BookProfile extends ImbuementProfileAsset {

    private static final int DEFAULT_SLOTS = 10;

    @Override
    public Map<String, PedestalSlot> resolveSlots(@Nullable ItemStack stored) {
        HexBookAsset book = CasterInventory.getHexBookAsset(stored);
        int count = book != null ? book.getSlotCount() : DEFAULT_SLOTS;
        Map<String, PedestalSlot> pages = new LinkedHashMap<>();
        for (int i = 1; i <= count; i++) {
            pages.put(String.valueOf(i),
                    PedestalSlot.of("Page " + i, "Hex stored in book page " + i));
        }
        return pages;
    }

    public static final BuilderCodec<BookProfile> CODEC = BuilderCodec
            .builder(BookProfile.class, BookProfile::new, ImbuementProfileAsset.BASE_CODEC)
            .build();
}
