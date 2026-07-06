package com.riprod.hexcode.builtin.ums.interaction;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.riprod.hexcode.builtin.ums.assets.BaseElementInteraction;
import com.riprod.hexcode.builtin.ums.registry.UmsInteractionHandler;
import com.riprod.hexcode.builtin.ums.registry.UmsInteractionRegistry;
import com.riprod.hexcode.builtin.ums.registry.UmsReactionContext;
import com.riprod.hexcode.builtin.ums.validators.UmsInteractionKeyValidator;
import com.riprod.hexcode.utils.VfxUtil;

public final class BasicInteraction extends BaseElementInteraction {

    public static final String ID = "Basic";

    private String particleId;
    private String soundId;
    private String handlerId;

    public static final BuilderCodec<BasicInteraction> CODEC = BuilderCodec
            .builder(BasicInteraction.class, BasicInteraction::new, BaseElementInteraction.BASE_CODEC)
            .append(new KeyedCodec<>("Particle", Codec.STRING, true),
                    (c, v) -> c.particleId = v, c -> c.particleId)
            .add()
            .append(new KeyedCodec<>("Sound", Codec.STRING, true),
                    (c, v) -> c.soundId = v, c -> c.soundId)
            .add()
            .<String>append(new KeyedCodec<>("Handler", Codec.STRING, true),
                    (c, v) -> c.handlerId = v, c -> c.handlerId)
            .metadata(new UIEditor(new UIEditor.Dropdown("HexcodeUmsInteractionHandlers")))
            .addValidatorLate(() -> UmsInteractionKeyValidator.INSTANCE.late())
            .add()
            .build();

    public String getHandlerId() {
        return handlerId;
    }

    @Override
    public void apply(UmsReactionContext ctx) {
        Vector3d pos = ctx.hitPosition();
        if (pos != null) {
            if (particleId != null) VfxUtil.particle(particleId, pos, ctx.accessor());
            if (soundId != null) VfxUtil.sound(soundId, pos, ctx.accessor());
        }
        if (handlerId != null) {
            UmsInteractionHandler handler = UmsInteractionRegistry.get(handlerId);
            if (handler != null) handler.handle(ctx);
        }
    }
}
