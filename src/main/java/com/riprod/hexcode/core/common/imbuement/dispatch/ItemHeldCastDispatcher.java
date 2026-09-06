package com.riprod.hexcode.core.common.imbuement.dispatch;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.imbued.triggers.InteractionPayload;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.root.PlayerHexRoot;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
import com.riprod.hexcode.core.common.imbuement.extract.ItemStatExtractor;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementProfileUtils;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;
import com.riprod.hexcode.core.common.triggers.registry.Trigger;
import com.riprod.hexcode.utils.SpellMana;

public final class ItemHeldCastDispatcher implements CastRootDispatcher {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void dispatch(@Nonnull Trigger trigger, @Nonnull TriggerEvent event,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        Ref<EntityStore> player = event.subjectRef();
        if (player == null || !player.isValid()) return;

        ItemStack heldItem = resolveHeldItem(event, buffer, player);
        if (heldItem == null || heldItem.isEmpty()) return;
        if (ImbuementUtils.readAll(heldItem).isEmpty()) return;

        ImbuementData data = ImbuementUtils.read(heldItem, trigger.getId());
        if (data == null) return;
        Hex hex = ImbuementUtils.resolveHex(data, buffer);
        if (hex == null) return;

        PlayerHexRoot hexRoot = new PlayerHexRoot(player, buffer);
        float volatilityMax = ItemStatExtractor.extractVolatility(heldItem);
        float baseMana = SpellMana.computeTotalMana(hex, buffer);
        float resolvedPower = 1.0f + ItemStatExtractor.extractPower(heldItem);
        HexCast tracker = new HexCast();
        tracker.volatility().init(volatilityMax, 1.0f, resolvedPower);

        HexContext context = new HexContext(hex, baseMana, hexRoot, null, tracker);

        ImbuementProfileAsset profile = ImbuementProfileUtils.first(heldItem);
        HexConfigAsset profileDefaults = profile != null ? profile.getDefaults() : null;
        if (profileDefaults != null) profileDefaults.applyTo(context);
        HexConfigAsset overrides = data.getOverrides();
        if (overrides != null) overrides.applyTo(context);

        HexVar defaultVar = trigger.resolveDefaultVariable(event);
        if (defaultVar != null) context.setDefaultVariable(defaultVar);
        context.setCastSlotKey(trigger.getId());

        try {
            HexExecuter.cast(context, buffer);
        } catch (Exception e) {
            LOGGER.atSevere().log("%s imbuement dispatch failed: %s", trigger.getId(), e.getMessage());
        }
    }

    private static ItemStack resolveHeldItem(TriggerEvent event,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        if (event.payload() instanceof InteractionPayload ip && ip.itemInHand() != null) {
            return ip.itemInHand();
        }
        return InventoryComponent.getItemInHand(buffer, player);
    }
}
