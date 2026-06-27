package com.riprod.hexcode.builtin.hexCore.glyphs.bolt;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.api.imbuement.ImbuedBlockActivator;
import com.riprod.hexcode.builtin.hexCore.glyphs.bolt.style.BoltStyle;
import com.riprod.hexcode.utils.HexVarUtil;

public class BoltGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Bolt";

    private static int damageCauseIndex = -1;

    @Override
    public boolean consumeVolatility(Glyph glyph, HexContext hexContext) {
        HexStats tracker = hexContext.getVolatilityTracker();
        if (tracker == null)
            return true;

        HexVar magInput = glyph.readSlot(BoltGlyphSlots.POWER, hexContext);
        double magnitude = HexVarUtil.numberOrDefault(magInput, 15.0);

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getImpact();
        float cost = glyph.computeBaseCost() * Impact.scale(impact, magnitude);
        return tracker.consumeVolatility(cost) > 0f;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar target = glyph.readSlot(BoltGlyphSlots.TARGET, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        World world = accessor.getExternalData().getWorld();
        Vector3f color = BoltStyle.resolveColor(hexContext);

        EntityVar entityVar = HexVarUtil.resolveEntityVar(target, hexContext);
        BlockVar blockVar = entityVar == null ? HexVarUtil.resolveBlockVar(target, hexContext) : null;
        if (entityVar != null) {
            handleEntityTarget(glyph, hexContext, entityVar, accessor, world, color);
        } else if (blockVar != null) {
            handleBlockTarget(glyph, hexContext, blockVar, accessor, world, color);
        } else {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be an Entity or Block");
            return;
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void handleEntityTarget(Glyph glyph, HexContext hexContext, EntityVar entityVar,
            CommandBuffer<EntityStore> accessor,
            World world, Vector3f color) {

        Ref<EntityStore> targetRef = entityVar.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            LOGGER.atWarning().log("bolt: entity target ref invalid");
            return;
        }

        TransformComponent targetTc = accessor.getComponent(targetRef, TransformComponent.getComponentType());
        if (targetTc == null) {
            LOGGER.atWarning().log("bolt: target has no transform");
            return;
        }

        Vector3d targetPos = targetTc.getPosition();

        BoltStyle.renderImpact(accessor, targetPos, hexContext);
        BoltStyle.applyShockEffect(accessor, targetRef);

        double damageAmount = HexVarUtil.numberOrDefault(
                glyph.readSlot(BoltGlyphSlots.POWER, hexContext), 5.0);
        damageAmount *= hexContext.getMagicPowerMultiplier();
        applyDamage(accessor, targetRef, (float) damageAmount);

        LOGGER.atInfo().log("bolt: hit entity for %.1f damage", damageAmount);
    }

    private void handleBlockTarget(Glyph glyph, HexContext hexContext, BlockVar blockVar,
            CommandBuffer<EntityStore> accessor,
            World world, Vector3f color) {

        Vector3i blockPos = blockVar.getValue();
        if (blockPos == null) {
            LOGGER.atWarning().log("bolt: block target position is null");
            return;
        }

        Vector3d targetPos = new Vector3d(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5);

        BoltStyle.renderImpact(accessor, targetPos, hexContext);

        ImbuedBlockActivator.ActivationOutcome outcome = ImbuedBlockActivator.tryConsume(world, blockPos);
        if (!outcome.isReady()) {
            triggerBlockInteraction(accessor, hexContext.getCasterRef(accessor), world, blockPos);
        }

        glyph.writeOutput(new BlockVar(blockPos), hexContext);

        LOGGER.atInfo().log("bolt: hit block at %d %d %d (activation=%s)",
                blockPos.x, blockPos.y, blockPos.z, outcome.getStatus());
    }

    private void triggerBlockInteraction(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> casterRef, World world, Vector3i blockPos) {

        InteractionManager manager = accessor.getComponent(casterRef,
                InteractionModule.get().getInteractionManagerComponent());
        if (manager == null) {
            LOGGER.atInfo().log("bolt: no interaction manager on caster, skipping block interaction");
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
        BlockPosition blockPosition = new BlockPosition(blockPos.x, blockPos.y, blockPos.z);
        ctx.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, blockPosition);
        ctx.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, blockPosition);

        InteractionChain chain = manager.initChain(InteractionType.Use, ctx, rootInteraction, false);
        manager.queueExecuteChain(chain);
    }

    private static void applyDamage(CommandBuffer<EntityStore> accessor,
            Ref<EntityStore> targetRef, float amount) {
        if (damageCauseIndex < 0) {
            damageCauseIndex = DamageCause.getAssetMap().getIndex("Environment");
        }
        if (damageCauseIndex == Integer.MIN_VALUE) {
            LOGGER.atWarning().log("bolt: Environment damage cause not found");
            return;
        }

        DamageCause cause = DamageCause.getAssetMap().getAsset(damageCauseIndex);
        if (cause == null) {
            LOGGER.atWarning().log("bolt: could not resolve damage cause");
            return;
        }

        Damage damage = new Damage(new Damage.EnvironmentSource("hex_bolt"), cause, amount);
        DamageSystems.executeDamage(targetRef, accessor, damage);
    }
}
