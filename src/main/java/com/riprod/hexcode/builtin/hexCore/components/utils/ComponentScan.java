package com.riprod.hexcode.builtin.hexCore.components.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.components.component.ComponentCacheEntry;
import com.riprod.hexcode.core.common.hexes.codec.HexCacheResource;
import com.riprod.hexcode.core.common.hexes.codec.HexCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;

public final class ComponentScan {

    private static ComponentType<EntityStore, ? extends InventoryComponent>[] heldSections;

    private ComponentScan() {
    }

    public static List<ComponentCacheEntry> scan(ComponentAccessor<EntityStore> accessor,
            Ref<EntityStore> playerRef) {
        var entries = new ArrayList<ComponentCacheEntry>();
        scanContainer(InventoryComponent.getCombined(accessor, playerRef, heldSections()),
                accessor, entries);
        return entries;
    }

    public static boolean stillHeld(ComponentAccessor<EntityStore> accessor,
            Ref<EntityStore> playerRef, String sourceRaw) {
        return containsRaw(InventoryComponent.getCombined(accessor, playerRef, heldSections()),
                sourceRaw);
    }

    @SuppressWarnings("unchecked")
    private static ComponentType<EntityStore, ? extends InventoryComponent>[] heldSections() {
        if (heldSections == null) {
            heldSections = new ComponentType[]{
                    InventoryComponent.Hotbar.getComponentType(),
                    InventoryComponent.Utility.getComponentType()};
        }
        return heldSections;
    }

    private static void scanContainer(@Nullable ItemContainer container,
            ComponentAccessor<EntityStore> accessor, List<ComponentCacheEntry> entries) {
        if (container == null) return;
        for (short slot = 0; slot < container.getCapacity(); slot++) {
            ItemStack stack = container.getItemStack(slot);
            if (stack == null || stack.isEmpty()) continue;
            for (ImbuementData data : ImbuementUtils.readAll(stack).values()) {
                String raw = data.getHexCompressedId();
                if (raw == null || raw.isEmpty()) continue;
                collect(raw, accessor, entries);
            }
        }
    }

    private static void collect(String raw, ComponentAccessor<EntityStore> accessor,
            List<ComponentCacheEntry> entries) {
        try {
            var cache = accessor.getResource(HexCacheResource.getResourceType());
            Hex hex = cache != null ? cache.getOrDecode(raw) : HexUtils.deserialize(raw);
            if (hex == null || hex.getEncoding() == null || hex.getEncoding().isEmpty()) return;
            String canonical = HexCodec.canonicalizeSnapshot(raw);
            entries.add(new ComponentCacheEntry(hex.getEncoding(), canonical, raw));
        } catch (Exception ignored) {
        }
    }

    private static boolean containsRaw(@Nullable ItemContainer container, String sourceRaw) {
        if (container == null) return false;
        for (short slot = 0; slot < container.getCapacity(); slot++) {
            ItemStack stack = container.getItemStack(slot);
            if (stack == null || stack.isEmpty()) continue;
            for (ImbuementData data : ImbuementUtils.readAll(stack).values()) {
                if (sourceRaw.equals(data.getHexCompressedId())) return true;
            }
        }
        return false;
    }
}
