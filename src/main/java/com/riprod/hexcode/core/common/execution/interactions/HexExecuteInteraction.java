package com.riprod.hexcode.core.common.execution.interactions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffAsset;
import com.riprod.hexcode.core.common.hexstaff.component.HexStaffComponent;
import com.riprod.hexcode.utils.HexSlot;
import com.riprod.hexcode.utils.SpellMana;

public class HexExecuteInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexExecuteInteraction> CODEC = BuilderCodec
            .builder(HexExecuteInteraction.class, HexExecuteInteraction::new, SimpleInteraction.CODEC)
            .<String>appendInherited(
                    new KeyedCodec<>("Config", HexConfigAsset.CHILD_ASSET_CODEC, true),
                    (i, v) -> i.configId = v,
                    i -> i.configId,
                    (i, p) -> i.configId = p.configId)
            .addValidatorLate(() -> HexConfigAsset.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Nullable
    private String configId;

    @Nullable
    public String getConfigId() {
        return this.configId;
    }

    @Nullable
    public HexConfigAsset getConfig() {
        if (this.configId == null) {
            return null;
        }
        return HexConfigAsset.getAssetMap().getAsset(this.configId);
    }

    protected void tick0(boolean firstRun, float dt, @Nonnull InteractionType type, @Nonnull InteractionContext ctx,
            @Nonnull CooldownHandler cooldown) {
        if (!firstRun) {
            ctx.getState().state = InteractionState.Finished;
            return;
        }
        try {
            CommandBuffer<EntityStore> buffer = ctx.getCommandBuffer();
            Ref<EntityStore> ref = ctx.getEntity();
            if (buffer == null || ref == null || !ref.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            HexConfigAsset config = getConfig();
            if (config == null) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            PlayerHexRoot hexRoot = new PlayerHexRoot(ref, buffer);
            Hex hex = config.getHex(buffer, hexRoot);
            if (hex == null) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }

            Hex hexClone = hex.clone();
            HexUtils.validate(hexClone);

            HexStats cfgStats = config.getHexStats();
            float volatility = cfgStats.getInitialVolatility();
            float volMult = cfgStats.getVolatilityMultiplier();
            if (volMult <= 0f) volMult = 1.0f;

            float baseMana = SpellMana.computeTotalMana(hexClone);
            float resolvedPower = hexRoot.resolveSpellPower(buffer);

            HexStaffComponent staff = CasterInventory.getHexStaffComponent(buffer, ref);
            float castDecayRate = staff != null ? staff.getCastDecayRate() : 0f;

            HexStaffAsset staffAsset = CasterInventory.getHexStaffAsset(
                    PlayerUtils.getHandItem(buffer, ref, HexSlot.MainHand));
            HexBookAsset bookAsset = CasterInventory.getHexBookAsset(
                    PlayerUtils.getHandItem(buffer, ref, HexSlot.OffHand));

            HexStyleAsset style = HexStyleAsset.empty();
            if (staffAsset != null && staffAsset.getStyle() != null) style.compose(staffAsset.getStyle());
            if (config.getStyle() != null) style.compose(config.getStyle());
            if (bookAsset != null && bookAsset.getStyle() != null
                    && bookAsset.getStyle().getSecondaryColor() != null) {
                style.setSecondaryColor(bookAsset.getStyle().getSecondaryColor().clone());
            }

            HexStats tracker = new HexStats(volatility, volMult, resolvedPower);
            HexContext context = new HexContext(hexClone, baseMana, hexRoot, style, tracker);
            context.setCastDecayRate(castDecayRate);

            if (staffAsset != null) context.applyNonDefaultsFrom(staffAsset.getDefaults());
            if (bookAsset != null) context.applyNonDefaultsFrom(bookAsset.getDefaults());

            HexExecuter.cast(context, buffer);
            ctx.getState().state = InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexExecuteInteraction failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }
}
