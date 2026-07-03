package com.riprod.hexcode.core.common.pedestal.utils;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.utils.HexSlot;

import io.sentry.util.Pair;

public class PedestalItemUtil {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static boolean isEssence(@Nullable ItemStack item) {
        if (item == null || item.isEmpty() || item.getItem() == null) {
            return false;
        }
        return item.getItem().getId().contains("Essence");
    }

    public static boolean anyEssence(@Nullable ItemStack mainHand, @Nullable ItemStack offHand) {
        return isEssence(mainHand) || isEssence(offHand);
    }

    public static Pair<ItemStack, HexSlot> getEssence(@Nullable ItemStack mainHand, @Nullable ItemStack offHand) {
        if (isEssence(mainHand)) {
            return new Pair<>(mainHand, HexSlot.MainHand);
        } else if (isEssence(offHand)) {
            return new Pair<>(offHand, HexSlot.OffHand);
        } else {
            return null;
        }
    }

    public static boolean isHexBook(@Nullable ItemStack item) {
        return CasterInventory.getHexBookAsset(item) != null;
    }

    public static boolean anyHexBook(@Nullable ItemStack mainHand, @Nullable ItemStack offHand) {
        return isHexBook(mainHand) || isHexBook(offHand);
    }

    public static Pair<ItemStack, HexSlot> getHexBook(@Nullable ItemStack mainHand, @Nullable ItemStack offHand) {
        if (isHexBook(offHand)) {
            return new Pair<>(offHand, HexSlot.OffHand);
        } else if (isHexBook(mainHand)) {
            return new Pair<>(mainHand, HexSlot.MainHand);
        } else {
            return null;
        }
    }

    public static boolean isEmptyHand(@Nullable ItemStack item) {
        return item == null || item.isEmpty();
    }

    public static void returnBookToPlayer(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> ref,
            ItemStack bookStack) {
        returnBookToPlayer(accessor, ref, bookStack, HexSlot.MainHand);
    }

    public static void returnBookToPlayer(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> ref,
            ItemStack bookStack, HexSlot slot) {
        if (bookStack == null || bookStack.isEmpty()) {
            return;
        }
        PlayerUtils.addHandItem(accessor, ref, slot, bookStack);
    }

    public static boolean returnEssenceToPlayer(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> ref,
            @Nullable String essenceItemId) {
        if (essenceItemId == null) {
            return false;
        }
        InventoryComponent.Hotbar hotbar = accessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if (hotbar == null) return false;
        ItemStack essenceStack = new ItemStack(essenceItemId, 1);
        return hotbar.getInventory().addItemStack(essenceStack).succeeded();
    }

    @Nullable
    public static Ref<EntityStore> dropEssenceAtPosition(
            ComponentAccessor<EntityStore> accessor, @Nullable String essenceItemId, Vector3i blockPos) {
        if (essenceItemId == null) {
            return null;
        }
        ItemStack essenceStack = new ItemStack(essenceItemId, 1);
        Vector3d dropPos = new Vector3d(blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5);
        Holder<EntityStore> holder = ItemComponent.generateItemDrop(
                accessor, essenceStack, dropPos, new Rotation3f(), 0f, 0.2f, 0f);
        if (holder == null) {
            return null;
        }
        return accessor.addEntity(holder, AddReason.SPAWN);
    }

    @Nullable
    public static Ref<EntityStore> dropBookAtPosition(
            ComponentAccessor<EntityStore> accessor, ItemStack bookStack, Vector3i blockPos) {
        if (bookStack == null || bookStack.isEmpty()) {
            return null;
        }

        Vector3d dropPos = new Vector3d(blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5);
        Holder<EntityStore> holder = ItemComponent.generateItemDrop(
                accessor, bookStack, dropPos, new Rotation3f(), 0f, 0.2f, 0f);
        if (holder == null) {
            return null;
        }

        return accessor.addEntity(holder, AddReason.SPAWN);
    }
}
