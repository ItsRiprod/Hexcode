package com.riprod.hexcode.core.common.drawing.interactions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.casting.registry.CastingStyleValidator;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;

public class HexDrawMode extends ChargingInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexDrawMode> CODEC = BuilderCodec
            .builder(HexDrawMode.class, HexDrawMode::new, ChargingInteraction.CODEC)
            .appendInherited(new KeyedCodec<>("AuraParticles", ModelParticle.ARRAY_CODEC),
                    (i, v) -> i.auraParticles = v,
                    i -> i.auraParticles,
                    (i, p) -> i.auraParticles = p.auraParticles)
            .add()
            .appendInherited(new KeyedCodec<>("CastStyleId", Codec.STRING),
                    (i, v) -> i.castStyleId = v,
                    i -> i.castStyleId,
                    (i, p) -> i.castStyleId = p.castStyleId)
            .metadata(new UIEditor(new UIEditor.Dropdown("HexcodeCastingStyles")))
            .addValidatorLate(() -> CastingStyleValidator.INSTANCE.late())
            .add()
            .build();

    @Nullable
    private ModelParticle[] auraParticles;
    @Nullable
    private String castStyleId;

    public HexDrawMode() {
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Client;
    }

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
        try {
            CommandBuffer<EntityStore> buffer = ctx.getCommandBuffer();
            Ref<EntityStore> player = ctx.getOwningEntity();
            if (buffer == null || player == null || !player.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            if (firstRun && buffer.getComponent(player, DrawCaptureComponent.getComponentType()) == null) {
                String castStyle = this.castStyleId != null ? this.castStyleId : "ring";
                buffer.putComponent(player, DrawCaptureComponent.getComponentType(),
                        new DrawCaptureComponent(this.auraParticles, castStyle));
            }

            super.tick0(firstRun, time, type, ctx, cooldown);

            if (ctx.getState().state != InteractionState.NotFinished) {
                buffer.tryRemoveComponent(player, DrawCaptureComponent.getComponentType());
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexDrawMode failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }
}
