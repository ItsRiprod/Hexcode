package com.riprod.hexcode.builtin.hextreme.imbuement;

import java.util.Map;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.ItemDisplayMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.builtin.hextreme.execution.config.PageConfig;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.imbuement.asset.profiles.StaticSlotProfile;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;

public final class PageProfile extends StaticSlotProfile {

    private String emptyStateKey = "Empty";
    private boolean restoreDurabilityOnWrite = true;

    public String getEmptyStateKey() {
        return emptyStateKey;
    }

    @Nullable
    public String getSlotKey() {
        Map<String, PedestalSlot> slots = resolveSlots(null);
        return slots.isEmpty() ? null : slots.keySet().iterator().next();
    }

    @Override
    public ItemStack writeHex(ItemStack stored, String slotKey, @Nullable Hex hex) {
        ItemStack out = super.writeHex(stored, slotKey, hex);
        if (out == null || out.isEmpty()) {
            return out;
        }
        if (hex == null || hex.getGlyphs().isEmpty()) {
            return demote(out);
        }
        return named(promote(out), hex.getDisplayName());
    }

    private static ItemStack named(ItemStack page, @Nullable String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return page;
        }
        return page.withMetadata(ItemDisplayMetadata.KEYED_CODEC,
                new ItemDisplayMetadata(Message.raw(displayName), null));
    }

    private ItemStack promote(ItemStack page) {
        String baseId = PageStateResolver.resolveBase(page, emptyStateKey);
        if (baseId == null) {
            return page;
        }
        Item base = Item.getAssetMap().getAsset(baseId);
        double maxDurability = base != null ? base.getMaxDurability() : page.getMaxDurability();
        double durability = restoreDurabilityOnWrite ? maxDurability : page.getDurability();
        return new ItemStack(baseId, page.getQuantity(), durability, maxDurability, page.getMetadata());
    }

    private ItemStack demote(ItemStack page) {
        ItemStack cleared = page
                .withMetadata(PageConfig.METADATA_KEY, Codec.STRING, null)
                .withMetadata(ItemDisplayMetadata.KEYED_CODEC, null);
        Item item = cleared.getItem();
        if (item == null || item.isState() || item.getItemIdForState(emptyStateKey) == null) {
            return cleared;
        }
        ItemStack empty = cleared.withState(emptyStateKey);
        Item emptyItem = empty.getItem();
        return emptyItem != null ? empty.withRestoredDurability(emptyItem.getMaxDurability()) : empty;
    }

    public static final BuilderCodec<PageProfile> CODEC = slotCodecBuilder(PageProfile.class, PageProfile::new)
            .append(new KeyedCodec<>("EmptyStateKey", Codec.STRING),
                    (a, v) -> { if (v != null) a.emptyStateKey = v; },
                    a -> a.emptyStateKey)
            .documentation("Item State key holding the blank/spent variant of each page rarity. Writing a hex promotes the blank to its base item; clearing a hex demotes it back.")
            .add()
            .append(new KeyedCodec<>("RestoreDurabilityOnWrite", Codec.BOOLEAN),
                    (a, v) -> { if (v != null) a.restoreDurabilityOnWrite = v; },
                    a -> a.restoreDurabilityOnWrite)
            .documentation("When true a newly inscribed page comes back at the base item's full charges. When false it keeps whatever durability the blank carried.")
            .add()
            .build();
}
