package com.riprod.hexcode.builtin.hexCore.glyphs.effects.interact;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.api.imbuement.ImbuedBlockActivator;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.interact.style.InteractStyle;
import com.riprod.hexcode.utils.HexVarUtil;

public class InteractGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String ID = "Interact";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(InteractConfig.class, InteractConfig.CODEC);
    }

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

        EntityVar entityVar = HexVarUtil.resolveEntityVar(target, hexContext);
        BlockVar blockVar = entityVar == null ? HexVarUtil.resolveBlockVar(target, hexContext) : null;
        if (entityVar != null) {
            handleEntityTarget(entityVar, accessor, hexContext);
        } else if (blockVar != null) {
            handleBlockTarget(glyph, hexContext, blockVar, accessor, world);
        } else {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be an Entity or Block");
            return;
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
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

        InteractStyle.renderImpact(accessor, targetTc.getPosition(), hexContext);
    }

    private void handleBlockTarget(Glyph glyph, HexContext hexContext, BlockVar blockVar,
            CommandBuffer<EntityStore> accessor, World world) {

        Vector3i blockPos = blockVar.getValue();
        if (blockPos == null) {
            LOGGER.atWarning().log("interact: block target position is null");
            return;
        }

        Vector3d targetPos = new Vector3d(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5);
        InteractStyle.renderImpact(accessor, targetPos, hexContext);

        ImbuedBlockActivator.ActivationOutcome outcome = ImbuedBlockActivator.tryConsume(world, blockPos);
        if (!outcome.isReady()) {
            triggerBlockInteraction(glyph, accessor, hexContext.getCasterRef(accessor), world, blockPos);
        }

        glyph.writeOutput(new BlockVar(blockPos), hexContext);

        LOGGER.atInfo().log("interact: hit block at %d %d %d (activation=%s)",
                blockPos.x, blockPos.y, blockPos.z, outcome.getStatus());
    }

    private void triggerBlockInteraction(Glyph glyph, CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> casterRef, World world, Vector3i blockPos) {

        InteractionManager manager = accessor.getComponent(casterRef,
                InteractionModule.get().getInteractionManagerComponent());
        if (manager == null) {
            LOGGER.atInfo().log("interact: no interaction manager on caster, skipping block interaction");
            return;
        }

        BlockType blockType = world.getBlockType(blockPos);
        if (blockType == null || blockType.isUnknown()) {
            return;
        }

        String interactionId = blockType.getInteractions().get(InteractionType.Use);
        if (interactionId == null) {
            return;
        }

        RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(interactionId);
        if (rootInteraction == null) {
            return;
        }

        InteractionContext ctx = InteractionContext.forInteraction(
                manager, casterRef, InteractionType.Use, accessor);

        InteractConfig config = getConfig(InteractConfig.class,
                GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId()));
        if (config != null && config.getReachProxyItem() != null) {
            ctx.setHeldItem(new ItemStack(config.getReachProxyItem(), 1));
        }

        BlockPosition blockPosition = new BlockPosition(blockPos.x, blockPos.y, blockPos.z);
        ctx.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, blockPosition);
        ctx.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, blockPosition);

        InteractionChain chain = manager.initChain(InteractionType.Use, ctx, rootInteraction, false);
        manager.queueExecuteChain(chain);
    }
}
