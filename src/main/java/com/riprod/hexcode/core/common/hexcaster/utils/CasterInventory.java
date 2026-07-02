package com.riprod.hexcode.core.common.hexcaster.utils;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexbook.component.HexBookComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffAsset;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffComponent;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.utils.HexSlot;
import com.riprod.hexcode.utils.HexStaffUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.sentry.util.Pair;

public class CasterInventory {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final String METADATA_KEY_HEX_STAFF = "HexStaff";
    public static final String METADATA_KEY_HEX_BOOK = "HexBook";

    public static boolean addHexToBook(ComponentAccessor<EntityStore> store,
            Ref<EntityStore> playerRef, HexSlot slot, Hex hex) {
        Pair<ItemStack, HexSlot> pair = PlayerUtils.getItemFromInventory(store, playerRef, slot, true);
        if (pair == null) return false;
        ItemStack item = pair.getFirst();
        if (item == null || item.isEmpty() || getHexBookAsset(item) == null) return false;
        Map<String, ImbuementData> map = new LinkedHashMap<>(ImbuementUtils.readAll(item));
        String key = nextFreeBookSlot(item, map);
        if (key == null) return false;
        map.put(key, ImbuementUtils.fromHex(hex));
        PlayerUtils.setHandItem(store, playerRef, pair.getSecond(), ImbuementUtils.writeAll(item, map));
        return true;
    }

    public static boolean removeGlyphFromBook(ComponentAccessor<EntityStore> store,
            Ref<EntityStore> playerRef, HexSlot slot, String glyphAssetId) {
        Pair<ItemStack, HexSlot> pair = PlayerUtils.getItemFromInventory(store, playerRef, slot, true);
        if (pair == null) return false;
        ItemStack item = pair.getFirst();
        if (item == null || item.isEmpty() || getHexBookAsset(item) == null) return false;
        Map<String, ImbuementData> map = new LinkedHashMap<>(ImbuementUtils.readAll(item));
        String found = null;
        for (Map.Entry<String, ImbuementData> e : map.entrySet()) {
            Hex h = ImbuementUtils.resolveHex(e.getValue(), store);
            if (h != null && h.getGlyphs().stream().anyMatch(g -> glyphAssetId.equals(g.getGlyphId()))) {
                found = e.getKey();
                break;
            }
        }
        if (found == null) return false;
        map.remove(found);
        PlayerUtils.setHandItem(store, playerRef, pair.getSecond(), ImbuementUtils.writeAll(item, map));
        return true;
    }

    private static List<Hex> resolveBookHexes(ComponentAccessor<EntityStore> store,
            Map<String, ImbuementData> map) {
        List<Map.Entry<String, ImbuementData>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey, slotKeyComparator()));
        List<Hex> hexes = new ArrayList<>(entries.size());
        for (Map.Entry<String, ImbuementData> e : entries) {
            Hex h = ImbuementUtils.resolveHex(e.getValue(), store);
            if (h != null) hexes.add(h);
        }
        return hexes;
    }

    @Nullable
    private static String nextFreeBookSlot(ItemStack item, Map<String, ImbuementData> map) {
        ImbuementProfileAsset profile = ImbuementUtils.resolveProfile(item);
        if (profile == null) return null;
        for (String key : profile.getSlots().keySet()) {
            if (!map.containsKey(key)) return key;
        }
        return null;
    }

    public static void saveHexStaffComponent(ComponentAccessor<EntityStore> store,
            Ref<EntityStore> playerRef, HexStaffComponent component) {
        ItemStack mainHandItem = PlayerUtils.getHandItem(store, playerRef, HexSlot.MainHand);
        if (mainHandItem == null || mainHandItem.isEmpty()) return;
        ItemStack newStack = mainHandItem.withMetadata(METADATA_KEY_HEX_STAFF, HexStaffComponent.CODEC, component);
        PlayerUtils.setHandItem(store, playerRef, HexSlot.MainHand, newStack);
    }

    @Nullable
    public static HexStaffComponent getHexStaffComponent(ComponentAccessor<EntityStore> store,
            Ref<EntityStore> playerRef) {
        ItemStack mainHandItem = InventoryComponent.getItemInHand(store, playerRef);
        if (mainHandItem == null || mainHandItem.isEmpty()) {
            return null;
        }

        HexStaffComponent existingComponent = mainHandItem.getFromMetadataOrNull(METADATA_KEY_HEX_STAFF,
                HexStaffComponent.CODEC);
        if (existingComponent != null) {
            return existingComponent;
        }

        HexStaffAsset staffAsset = getHexStaffAsset(mainHandItem);
        if (staffAsset == null && !HexStaffUtil.isHexStaff(mainHandItem)) {
            return null;
        }

        HexStaffComponent newComponent = staffAsset != null
                ? new HexStaffComponent(staffAsset)
                : new HexStaffComponent();
        ItemStack newStack = mainHandItem.withMetadata(METADATA_KEY_HEX_STAFF, HexStaffComponent.CODEC, newComponent);

        PlayerUtils.setHandItem(store, playerRef, HexSlot.MainHand, newStack);

        return newComponent;
    }

    @Nullable
    public static HexStaffAsset getHexStaffAsset(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty())
            return null;
        Item item = itemStack.getItem();
        if (item == null)
            return null;
        return HexStaffAsset.getAssetMap().getAsset(item.getId());
    }

    public static List<Hex> getHexesForCasting(ComponentAccessor<EntityStore> store,
            Ref<EntityStore> playerRef) {
        Pair<ItemStack, HexSlot> pair = PlayerUtils.getItemFromInventory(store, playerRef, HexSlot.OffHand, true);
        if (pair == null) return List.of();
        ItemStack stack = pair.getFirst();
        if (stack == null || stack.isEmpty()) return List.of();

        Map<String, ImbuementData> existing = ImbuementUtils.readAll(stack);
        if (existing.isEmpty()) {
            HexBookComponent book = stack.getFromMetadataOrNull(METADATA_KEY_HEX_BOOK, HexBookComponent.CODEC);
            if (book != null && !book.getHexes().isEmpty()) {
                Map<String, ImbuementData> migrated = new HashMap<>();
                List<Hex> legacyHexes = book.getHexes();
                for (int i = 0; i < legacyHexes.size(); i++) {
                    Hex h = legacyHexes.get(i);
                    if (h != null && !h.getGlyphs().isEmpty()) {
                        migrated.put(String.valueOf(i + 1), ImbuementUtils.fromHex(h));
                    }
                }
                if (!migrated.isEmpty()) {
                    ItemStack upgraded = ImbuementUtils.writeAll(stack, migrated);
                    PlayerUtils.setHandItem(store, playerRef, pair.getSecond(), upgraded);
                    existing = migrated;
                }
            }
        }
        if (existing.isEmpty()) return List.of();

        return resolveBookHexes(store, existing);
    }

    private static Comparator<String> slotKeyComparator() {
        return (a, b) -> {
            Integer ia = tryParseInt(a);
            Integer ib = tryParseInt(b);
            if (ia != null && ib != null) return Integer.compare(ia, ib);
            if (ia != null) return -1;
            if (ib != null) return 1;
            return a.compareTo(b);
        };
    }

    @Nullable
    private static Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    public static HexBookAsset getHexBookAsset(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty())
            return null;
        Item item = itemStack.getItem();
        if (item == null)
            return null;
        return HexBookAsset.getAssetMap().getAsset(item.getId());
    }
}
