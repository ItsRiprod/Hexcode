package com.riprod.hexcode.builtin.hexCore.glyphs.erode;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.erode.style.ErodeStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

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
        if (config == null) config = ErodeConfig.DEFAULTS;

        double amount = Math.max(config.getMinAmount(), Math.min(config.getMaxAmount(),
                HexVarUtil.numberOrSlotDefault(
                        glyph.readSlot(ErodeGlyphSlots.AMOUNT, hexContext),
                        asset.getSlot(ErodeGlyphSlots.AMOUNT))));
        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ErodeGlyphSlots.DURATION, hexContext),
                asset.getSlot(ErodeGlyphSlots.DURATION));
        float vulnerabilityMultiplier = (float) (amount * config.getVulnerabilityScale());
        float durationSeconds = (float) duration;

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar != null) {
            applyToEntities(entityVar, vulnerabilityMultiplier, durationSeconds, config,
                    glyph, hexContext, accessor);
        } else {
            BlockVar blockVar = HexVarUtil.resolveBlockVar(targets, hexContext);
            if (blockVar != null)
                applyToBlocks(blockVar, amount, config, hexContext, accessor);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }

    private void applyToEntities(EntityVar entityVar, float vulnerabilityMultiplier,
            float durationSeconds, ErodeConfig config, Glyph glyph, HexContext hexContext,
            CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid())
            return;

        String effectId = config.getEffectId();
        EntityEffect erodeEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (erodeEffect == null) {
            LOGGER.atWarning().log("erode: %s effect asset not found", effectId);
            return;
        }

        EffectControllerComponent controller = accessor.getComponent(
                ref, EffectControllerComponent.getComponentType());
        if (controller != null) {
            controller.addEffect(ref, erodeEffect, durationSeconds,
                    OverlapBehavior.OVERWRITE, accessor);
        }

        ErodeState existing = ConstructStateUtil.findState(
                accessor, ref, ErodeGlyph.ID, ErodeState.class);
        if (existing != null) {
            existing.setVulnerabilityMultiplier(vulnerabilityMultiplier);
            existing.setRemainingDuration(durationSeconds);
            existing.setNextGlyphIds(glyph.getNextLinks());
            existing.setEffectId(effectId);
        } else {
            ErodeState state = new ErodeState(vulnerabilityMultiplier, durationSeconds, glyph.getNextLinks());
            state.setEffectId(effectId);
            HexConstructSpawner.applyWithState(
                    accessor, ref, hexContext, glyph, ErodeGlyph.ID, state);
        }

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            ErodeStyle.renderEntityHit(tc.getPosition(), hexContext, accessor);
        }

        LOGGER.atInfo().log("erode: applied %.0f%% vulnerability for %.1fs to entity",
                vulnerabilityMultiplier * 100, durationSeconds);
    }

    private void applyToBlocks(BlockVar blockVar, double amount, ErodeConfig config,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Vector3i pos = blockVar.getValue();
        if (pos == null)
            return;

        ChunkStore chunkStore = accessor.getExternalData().getWorld().getChunkStore();
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
