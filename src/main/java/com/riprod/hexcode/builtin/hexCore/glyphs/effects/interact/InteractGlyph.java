package com.riprod.hexcode.builtin.hexCore.glyphs.effects.interact;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.api.imbuement.ImbuedBlockActivator;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.BlockUtils;
import com.riprod.hexcode.utils.LogScopes;

public class InteractGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    public static final String TYPE = "Interact";

    @Nullable
    private String reachProxyItem;

    public static final BuilderCodec<InteractGlyph> CODEC = BuilderCodec
            .builder(InteractGlyph.class, InteractGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("ReachProxyItem",
                    new ContainedAssetCodec<>(Item.class, Item.CODEC), true),
                    (c, v) -> c.reachProxyItem = v, c -> c.reachProxyItem)
            .addValidatorLate(() -> Item.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar target = glyph.readSlot(InteractGlyphSlots.TARGET, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        World world = accessor.getExternalData().getWorld();

        if (target instanceof EntityVar entityVar) {
            handleEntityTarget(entityVar, accessor, hexContext);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        if (target instanceof BlockVar blockVar) {
            handleBlockTarget(glyph, hexContext, blockVar, accessor, world);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                "Target must be an Entity or Block");

    }

    private void handleEntityTarget(EntityVar entityVar, CommandBuffer<EntityStore> accessor,
            HexContext hexContext) {

        Ref<EntityStore> targetRef = entityVar.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            LOGGER.atWarning().log("interact: entity target ref invalid");
            return;
        }

        TransformComponent targetTc = accessor.getComponent(targetRef, TransformComponent.getComponentType());
        if (targetTc == null) {
            LOGGER.atWarning().log("interact: target has no transform");
            return;
        }

        EffectRenderer.render(EffectSlot.Entity, this, hexContext, targetTc.getPosition(), accessor);
    }

    private void handleBlockTarget(Glyph glyph, HexContext hexContext, BlockVar blockVar,
            CommandBuffer<EntityStore> accessor, World world) {

        Vector3i blockPos = blockVar.getValue();
        if (blockPos == null) {
            LOGGER.atWarning().log("interact: block target position is null");
            return;
        }

        EffectRenderer.render(EffectSlot.Block, this, hexContext, EffectRenderer.blockCenter(blockPos), accessor);

        ImbuedBlockActivator.ActivationOutcome outcome = ImbuedBlockActivator.tryConsume(world, blockPos,
                hexContext.getRootRef(), accessor);
        if (!outcome.isReady()) {
            BlockUtils.triggerUseInteraction(accessor, hexContext.getRootRef(), world, blockPos, null);
        }

        glyph.writeOutput(new BlockVar(blockPos), hexContext);

        LOGGER.atFine().log("interact: hit block at %d %d %d (activation=%s)",
                blockPos.x, blockPos.y, blockPos.z, outcome.getStatus());
    }
}
