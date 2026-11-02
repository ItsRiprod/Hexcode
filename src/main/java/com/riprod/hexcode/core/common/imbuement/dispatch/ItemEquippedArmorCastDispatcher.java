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
import com.riprod.hexcode.core.common.execution.cast.component.CastHexComponent;
import com.riprod.hexcode.core.common.execution.cast.component.CastSlotComponent;
import com.riprod.hexcode.core.common.execution.cast.component.DefaultVariableComponent;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastHolder;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
import com.riprod.hexcode.core.common.imbuement.extract.ItemStatExtractor;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementProfileUtils;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;
import com.riprod.hexcode.core.common.triggers.registry.Trigger;

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

            ImbuementProfileAsset profile = ImbuementProfileUtils.first(stack);
            if (profile == null || profile.findSlot(stack, trigger.getId()) == null) continue;

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
        var holder = new CastHolder(player);
        HexConfigAsset profileDefaults = profile != null ? profile.getDefaults() : null;
        if (profileDefaults != null) profileDefaults.queueOnto(holder, buffer);

        float volatilityMax = ItemStatExtractor.extractVolatility(stack);
        var stats = new VolatilityComponent();
        stats.setInitial(volatilityMax > 0f ? volatilityMax : -1f);
        holder.overlay(VolatilityComponent.TYPE, stats);

        HexConfigAsset overrides = data.getOverrides();
        if (overrides != null) overrides.queueOnto(holder, buffer);

        holder.put(CastHexComponent.TYPE, new CastHexComponent(hex));
        HexVar defaultVar = trigger.resolveDefaultVariable(event);
        if (defaultVar != null) holder.put(DefaultVariableComponent.TYPE, new DefaultVariableComponent(defaultVar));
        holder.put(CastSlotComponent.TYPE, new CastSlotComponent(trigger.getId()));

        try {
            HexExecuter.cast(holder, buffer);
        } catch (Exception e) {
            LOGGER.atSevere().log("%s armor imbuement dispatch failed: %s", trigger.getId(), e.getMessage());
        }
    }
}
