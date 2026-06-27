package com.riprod.hexcode.core.common.imbuement.dispatch;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
import com.riprod.hexcode.core.common.imbuement.extract.ItemStatExtractor;
import com.riprod.hexcode.core.common.imbuement.registry.ImbuementProfileRegistry;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;
import com.riprod.hexcode.core.common.triggers.registry.Trigger;
import com.riprod.hexcode.utils.SpellMana;

public final class ItemEquippedArmorCastDispatcher implements CastRootDispatcher {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void dispatch(@Nonnull Trigger trigger, @Nonnull TriggerEvent event,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        Ref<EntityStore> player = event.subjectRef();
        if (player == null || !player.isValid()) return;

        Store<EntityStore> store = player.getStore();
        InventoryComponent.Armor armor = store.getComponent(player, InventoryComponent.Armor.getComponentType());
        if (armor == null) return;

        ItemContainer container = armor.getInventory();
        if (container == null) return;

        short capacity = container.getCapacity();
        for (short i = 0; i < capacity; i++) {
            ItemStack stack = container.getItemStack(i);
            if (stack == null || stack.isEmpty()) continue;
            if (ImbuementUtils.readAll(stack).isEmpty()) continue;

            ImbuementProfileAsset profile = ImbuementProfileRegistry.first(stack);
            if (profile == null || profile.findSlot(trigger.getId()) == null) continue;

            ImbuementData data = ImbuementUtils.read(stack, trigger.getId());
            if (data == null) continue;
            Hex hex = ImbuementUtils.resolveHex(data, buffer);
            if (hex == null) continue;

            fireOne(trigger, event, buffer, player, stack, hex, data, profile);
        }
    }

    private void fireOne(Trigger trigger, TriggerEvent event,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> player, ItemStack stack,
            Hex hex, ImbuementData data, ImbuementProfileAsset profile) {
        PlayerHexRoot hexRoot = new PlayerHexRoot(player, buffer);
        float volatilityMax = ItemStatExtractor.extractVolatility(stack);
        float baseMana = SpellMana.computeTotalMana(hex);
        float resolvedPower = 1.0f + ItemStatExtractor.extractPower(stack);
        HexStats tracker = new HexStats(volatilityMax, 1.0f, resolvedPower);

        HexContext context = new HexContext(hex, baseMana, hexRoot, null, tracker);

        if (profile != null) context.applyNonDefaultsFrom(profile.getDefaults());
        context.applyNonDefaultsFrom(data.getOverrides());

        HexVar defaultVar = trigger.resolveDefaultVariable(event);
        if (defaultVar != null) context.setDefaultVariable(defaultVar);
        context.setCastSlotKey(trigger.getId());

        try {
            HexExecuter.cast(context, buffer);
        } catch (Exception e) {
            LOGGER.atSevere().log("%s armor imbuement dispatch failed: %s", trigger.getId(), e.getMessage());
        }
    }
}
