package com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode.style.ErodeStyle;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.utils.HexVarUtil;

import java.util.Arrays;

public class ErodeGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Erode";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ErodeConfig.class, ErodeConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(ErodeGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Erode: target required");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ErodeConfig config = getConfig(ErodeConfig.class, asset);
        if (config == null)
            config = ErodeConfig.DEFAULTS;

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ErodeGlyphSlots.DURATION, hexContext),
                asset.getSlot(ErodeGlyphSlots.DURATION));

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar != null) {
            // sustained entity vulnerability defers continuation to the construct's onEnd
            if (config.canImpactEntities() && applyToEntities(glyph, entityVar, (float) duration, config, hexContext, accessor)) {
                Slot immediate = glyph.getSlot(ErodeGlyphSlots.IMMEDIATE);
                if (immediate != null && immediate.getLinks().length > 0) {
                    HexContext immediateCtx = hexContext.branch();
                    immediateCtx.setDefaultVariable(entityVar);
                    HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
                }
                return;
            }
        } else {
            BlockVar blockVar = HexVarUtil.resolveBlockVar(targets, hexContext);
            if (blockVar != null) {
                double amount = HexVarUtil.numberOrSlotDefault(
                        glyph.readSlot(ErodeGlyphSlots.AMOUNT, hexContext),
                        asset.getSlot(ErodeGlyphSlots.AMOUNT));
                applyToBlocks(blockVar, amount, config, hexContext, accessor);
            }
        }
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private boolean applyToEntities(Glyph glyph, EntityVar entityVar, float durationSeconds,
            ErodeConfig config, HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid())
            return false;

        World world = accessor.getExternalData().getWorld();
        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (!HexProtection.canAffectEntity(world, caster, accessor, ref)) {
            HexProtection.notifyBlocked(caster, accessor, getId());
            return false;
        }

        String effectId = config.getEffectId();
        EntityEffect erodeEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (erodeEffect == null) {
            LOGGER.atWarning().log("erode: %s effect asset not found", effectId);
            return false;
        }

        VfxUtil.applyBoundedEffect(hexContext, ref, glyph, effectId, durationSeconds,
                OverlapBehavior.OVERWRITE);

        ErodeState existing = ConstructStateUtil.findState(
                accessor, ref, ErodeGlyph.ID, ErodeState.class);
        if (existing != null) {
            existing.setRemainingDuration(durationSeconds);
            existing.setNextGlyphIds(glyph.getNextLinks());
        } else {
            ErodeState state = new ErodeState(durationSeconds, effectId, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, ref, hexContext, glyph, ErodeGlyph.ID, state);
        }

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            ErodeStyle.renderEntityHit(tc.getPosition(), hexContext, accessor);
        }

        LOGGER.atInfo().log("erode: applied vulnerability effect for %.1fs to entity", durationSeconds);
        return true;
    }

    private void applyToBlocks(BlockVar blockVar, double amount, ErodeConfig config,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Vector3i pos = blockVar.getValue();
        if (pos == null)
            return;

        World world = accessor.getExternalData().getWorld();
        if (!HexProtection.isBlockActionAllowed(world, BlockAction.BREAK)) {
            HexProtection.notifyBlocked(hexContext.getCasterRef(accessor), accessor, getId());
            return;
        }

        ChunkStore chunkStore = world.getChunkStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
        if (chunkRef == null || !chunkRef.isValid())
            return;

        int tier = amountToTier(amount, config);
        String toolId = config.getToolAssetPrefix() + tier;
        Item toolItem = Item.getAssetMap().getAsset(toolId);
        ItemTool tool = toolItem != null ? toolItem.getTool() : null;
        if (tool == null) {
            LOGGER.atWarning().log("erode: missing tool asset %s; block path no-op", toolId);
            return;
        }

        Ref<EntityStore> casterRef = hexContext.getCasterRef(accessor);
        float damageScale = (float) (amount * config.getBlockDamageScale());

        BlockHarvestUtils.performBlockDamage(
                casterRef,
                pos,
                null,
                tool,
                null,
                false,
                damageScale,
                0,
                chunkRef,
                accessor,
                chunkStore.getStore());

        LOGGER.atInfo().log("erode: routed block hit at %s through harvest pipeline (tier=%d, scale=%.2f)",
                pos, tier, damageScale);
    }

    private static int amountToTier(double amount, ErodeConfig config) {
        int t = (int) Math.floor((amount - 1) / config.getTierBucketWidth()) + 1;
        return Math.max(config.getMinTier(), Math.min(config.getMaxTier(), t));
    }
}
