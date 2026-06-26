package com.riprod.hexcode.builtin.hexCore.glyphs.growth;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingStageData;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.growth.style.GrowthStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class GrowthGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Growth";

    private static final String GROWTH_EFFECT_ID = "Hexcode_Growth";
    private static final double DEFAULT_AMOUNT = 5.0;
    private static final double DEFAULT_DURATION = 10.0;
    private static final double MIN_AMOUNT = 1.0;
    private static final double MAX_AMOUNT = 20.0;
    private static final int BONEMEAL_RADIUS = 2;
    private static final float BONEMEAL_CHANCE = 0.35f;

    private static final String[] VEGETATION_BLOCKS = {
            "Plant_Grass_Short", "Plant_Grass_Tall",
            "Plant_Flower_Daisy", "Plant_Flower_Poppy",
            "Plant_Fern"
    };

    private static final String[] GRASS_DIRT_PREFIXES = {
            "Soil_Grass", "Soil_Dirt"
    };

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(GrowthGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        double amount = Math.max(MIN_AMOUNT, Math.min(MAX_AMOUNT,
                HexVarUtil.numberOrDefault(
                        glyph.readSlot(GrowthGlyphSlots.AMOUNT, hexContext), DEFAULT_AMOUNT)));

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar != null) {
            applyToEntity(entityVar, amount, glyph, hexContext, accessor);
            return;
        }

        BlockVar blockVar = HexVarUtil.resolveBlockVar(targets, hexContext);
        if (blockVar != null) applyToBlock(blockVar, amount, hexContext, accessor);
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void applyToEntity(EntityVar entityVar, double amount,
            Glyph glyph, HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        EntityEffect growthEffect = EntityEffect.getAssetMap().getAsset(GROWTH_EFFECT_ID);
        if (growthEffect == null) {
            LOGGER.atWarning().log("growth: %s effect asset not found", GROWTH_EFFECT_ID);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        double duration = HexVarUtil.numberOrDefault(
                glyph.readSlot(GrowthGlyphSlots.DURATION, hexContext), DEFAULT_DURATION);
        float durationSeconds = (float) duration;

        EffectControllerComponent controller = accessor.getComponent(
                ref, EffectControllerComponent.getComponentType());
        if (controller != null) {
            controller.addEffect(ref, growthEffect, durationSeconds,
                    OverlapBehavior.OVERWRITE, accessor);
        }

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            GrowthStyle.renderEntityHit(tc.getPosition(), hexContext, accessor);
        }

        GrowthState existing = ConstructStateUtil.findState(
                accessor, ref, GrowthGlyph.ID, GrowthState.class);
        if (existing != null) {
            existing.setRemainingDuration(durationSeconds);
            existing.setNextGlyphIds(glyph.getNextLinks());
        } else {
            GrowthState state = new GrowthState(durationSeconds, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, ref, hexContext, glyph, GrowthGlyph.ID, state);
        }

        LOGGER.atInfo().log("growth: applied regen buff for %.1fs to entity", durationSeconds);
    }

    private void applyToBlock(BlockVar blockVar, double amount,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Vector3i pos = blockVar.getValue();
        if (pos == null) return;

        World world = accessor.getExternalData().getWorld();
        int blockId = world.getBlock(pos.x, pos.y, pos.z);
        if (blockId == BlockType.EMPTY_ID) return;

        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null) return;

        if (tryAdvanceGrowth(world, pos, blockType, amount, hexContext, accessor)) {
            return;
        }

        if (isGrassDirtBlock(blockType)) {
            applyBonemeal(world, pos, amount, hexContext, accessor);
        }
    }

    private boolean tryAdvanceGrowth(World world, Vector3i pos, BlockType blockType,
            double amount, HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        FarmingData farmingConfig = blockType.getFarming();
        if (farmingConfig == null || farmingConfig.getStages() == null) return false;

        WorldChunk worldChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        if (worldChunk == null) return false;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(
                ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        if (chunkRef == null) return false;

        BlockComponentChunk blockComponentChunk = chunkStore.getComponent(
                chunkRef, BlockComponentChunk.getComponentType());
        if (blockComponentChunk == null) return false;

        int blockIndexColumn = ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z);
        Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndexColumn);
        if (blockRef == null || !blockRef.isValid()) return false;

        FarmingBlock farmingBlock = chunkStore.getComponent(blockRef, FarmingBlock.getComponentType());
        if (farmingBlock == null) return false;

        String stageSetName = farmingBlock.getCurrentStageSet();
        FarmingStageData[] stages = farmingConfig.getStages().get(stageSetName);
        if (stages == null || stages.length == 0) return false;

        int currentStage = (int) farmingBlock.getGrowthProgress();
        if (currentStage >= stages.length) currentStage = stages.length - 1;

        int stagesToAdvance = Math.max(1, (int) (amount / DEFAULT_AMOUNT));
        int newStage = Math.min(currentStage + stagesToAdvance, stages.length - 1);

        if (newStage <= currentStage) return true;

        WorldTimeResource worldTimeResource = world.getEntityStore().getStore()
                .getResource(WorldTimeResource.getResourceType());
        Instant now = worldTimeResource.getGameTime();

        FarmingStageData previousStage = (currentStage >= 0 && currentStage < stages.length)
                ? stages[currentStage] : null;

        farmingBlock.setGrowthProgress(newStage);
        farmingBlock.setExecutions(0);
        farmingBlock.setGeneration(farmingBlock.getGeneration() + 1);
        farmingBlock.setLastTickGameTime(now);

        Ref<ChunkStore> sectionRef = world.getChunkStore()
                .getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (sectionRef != null && sectionRef.isValid()) {
            BlockSection blockSection = chunkStore.getComponent(
                    sectionRef, BlockSection.getComponentType());
            if (blockSection != null) {
                blockSection.scheduleTick(ChunkUtil.indexBlock(pos.x, pos.y, pos.z), now);
            }
            stages[newStage].apply(chunkStore, sectionRef, blockRef, pos.x, pos.y, pos.z, previousStage);
        }

        worldChunk.setTicking(pos.x, pos.y, pos.z, true);

        Vector3d blockCenter = new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
        GrowthStyle.renderBlockHit(blockCenter, hexContext, accessor);

        LOGGER.atInfo().log("growth: advanced crop at %s from stage %d to %d", pos, currentStage, newStage);
        return true;
    }

    private boolean isGrassDirtBlock(BlockType blockType) {
        String id = blockType.getId();
        if (id == null) return false;
        for (String prefix : GRASS_DIRT_PREFIXES) {
            if (id.startsWith(prefix)) return true;
        }
        return false;
    }

    private void applyBonemeal(World world, Vector3i pos, double amount,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int attempts = Math.max(3, (int) (amount * 1.5));

        for (int a = 0; a < attempts; a++) {
            int dx = rng.nextInt(-BONEMEAL_RADIUS, BONEMEAL_RADIUS + 1);
            int dz = rng.nextInt(-BONEMEAL_RADIUS, BONEMEAL_RADIUS + 1);
            int tx = pos.x + dx;
            int tz = pos.z + dz;
            int ty = pos.y;

            int belowId = world.getBlock(tx, ty, tz);
            if (belowId == BlockType.EMPTY_ID) continue;

            int aboveId = world.getBlock(tx, ty + 1, tz);
            if (aboveId != BlockType.EMPTY_ID) continue;

            if (rng.nextFloat() > BONEMEAL_CHANCE) continue;

            String vegetation = VEGETATION_BLOCKS[rng.nextInt(VEGETATION_BLOCKS.length)];
            world.setBlock(tx, ty + 1, tz, vegetation);

            Vector3d effectPos = new Vector3d(tx + 0.5, ty + 1.5, tz + 0.5);
            GrowthStyle.renderBlockHit(effectPos, hexContext, accessor);
        }

        LOGGER.atInfo().log("growth: applied bonemeal around %s", pos);
    }
}
