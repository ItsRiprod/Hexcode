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
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.root.PlayerHexRoot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
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

            float baseMana = SpellMana.computeTotalMana(hexClone);

            HexContext context = new HexContext(hexClone, baseMana, hexRoot,
                    HexStyleAsset.empty(), new HexCast());
            config.applyTo(context);

            HexExecuter.cast(context, buffer);
            ctx.getState().state = InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexExecuteInteraction failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }
}
